/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.observations

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.Napier
import io.redlink.more.SharedRes
import io.redlink.more.database.entities.LatestObservationDataEntity
import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.extensions.asString
import io.redlink.more.extensions.desc
import io.redlink.more.extensions.formatted
import io.redlink.more.extensions.reminderId
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.longRunningObservation.LongRunningObservationStorage
import io.redlink.more.observations.observationTypes.ObservationType
import io.redlink.more.observations.polling.PollingObservationRegistry
import io.redlink.more.scopes.Scope
import io.redlink.more.scopes.StudyScope
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.services.store.PermissionApprovalState
import io.redlink.more.util.openSystemSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

interface Collector
interface PermissionCollector : Collector {
    val permissionKey: String
    suspend fun permissionState(): PermissionApprovalState
    suspend fun requestPermission()
}

interface BundledPermissionCollector : PermissionCollector {
    val permissionGroup: String
}

interface ObservationPermissionObserver {
    fun requestPermission(observationType: ObservationType)
    fun permissionState(observationType: ObservationType): PermissionApprovalState

    suspend fun permissionStates(
        collectors: Collection<PermissionCollector>
    ): Map<String, PermissionApprovalState> =
        collectors.associate { it.permissionKey to it.permissionState() }

    suspend fun requestPermissions(
        collectors: Collection<PermissionCollector>
    ) {
        collectors.forEach { it.requestPermission() }
    }
}

abstract class Observation(
    protected val repos: MainRepository,
    val observationType: ObservationType,
) {
    protected val permissionQueryMutex = Mutex()
    private val collectorPermissionQueryMutex = Mutex()
    private val errorQueryMutex = Mutex()

    protected var dataManager: ObservationDataManager? = null
        private set

    private var notificationManager: NotificationManager? = null

    private var permissionObserver: ObservationPermissionObserver? = null

    protected var running = false
    protected val observationIds = mutableSetOf<String>()

    protected var longRunningStorage: LongRunningObservationStorage? = null


    private val observationTypes = mutableMapOf<String, String>()
    protected val scheduleIds = mutableMapOf<String, String>()
    private val notificationIds = mutableMapOf<String, String>()
    private val config = mutableMapOf<String, Any>()
    private var configChanged = false

    protected var lastCollectionTimestamp: Instant? = null

    var timestampCollectionJob: Job? = null

    fun setPermissionObserver(observer: ObservationPermissionObserver?) {
        permissionObserver = observer
        Napier.d { "PermissionObserver set for ${observationTypes.values.joinToString(", ")}" }
    }

    fun setLongRunningObservationStorage(longRunningObservationStorage: LongRunningObservationStorage?) {
        this.longRunningStorage = longRunningObservationStorage
    }

    open suspend fun start(
        observationId: String,
        scheduleId: String,
        notificationId: String? = null
    ): Boolean {
        observationIds.add(observationId)
        val realObservationType =
            repos.observation.getObservationByObservationId(observationId)?.observationType
                ?: repos.observation.observationById(observationId).firstOrNull()?.observationType
                ?: observationType.observationType
        observationTypes[observationId] = realObservationType

        timestampCollectionJob?.cancel()
        timestampCollectionJob = StudyScope.launch {
            repos.observation.collectTimestampForObservationIds(observationIds).collect {
                lastCollectionTimestamp = Instant.fromEpochMilliseconds(it)
                Napier.d(tag = "Observation::start") { "Last collection $lastCollectionTimestamp" }
            }
        }.second
        timestampCollectionJob?.invokeOnCompletion {
            timestampCollectionJob = null
        }
        scheduleIds[scheduleId] = observationId
        notificationId?.let {
            notificationIds[scheduleId] = notificationId
        }
        if (running && configChanged) {
            stop {}
            running = false
        }
        configChanged = false
        return if (!running) {
            Napier.i(tag = "Observation::start") { "Observation with type ${observationType.observationType} starting..." }
            applyObservationConfig(config)
            permissionObserver?.let {
                updateObservationPermissions()
            }
            running = start()
            if (running) {
                activate()
            }
            Napier.i { "Observation with type ${observationType.observationType} started: $running" }
            running
        } else true
    }

    open fun stop(scheduleId: String, removeNotification: Boolean = false) {
        Napier.i(tag = "Observation::stop") { "Stopping observation of type ${observationType.observationType} for schedule $scheduleId." }
        if (scheduleIds.size <= 1) {
            stop {
                timestampCollectionJob?.cancel()
                saveAndSend()
                observationShutdown(scheduleId)
            }
        } else {
            saveAndSend()
            observationShutdown(scheduleId)
        }
        if (removeNotification) {
            handleNotification(scheduleId)
        }
        Scope.launch {
            updateObservationErrors()
        }
    }

    fun observationDataManagerAdded() = dataManager != null

    fun applyDataManager(observationDataManager: ObservationDataManager) {
        Napier.i(tag = "Observation::setDataManager") { "Setting data manager for observation of type ${observationType.observationType}." }
        dataManager = observationDataManager
    }

    fun setNotificationManager(notificationManager: NotificationManager) {
        this.notificationManager = notificationManager
    }

    fun addNotificationId(scheduleId: String, notificationId: String) {
        notificationIds[scheduleId] = notificationId
    }

    fun requestPermission() {
        if (isPermissionRequested(observationType.observationType)) {
            Napier.d(tag = "Observation::requestPermission") { "Permission already requested for ${observationType.observationType} in this session, skipping..." }
            return
        }
        markPermissionRequested(observationType.observationType)
        if (permissionObserver == null) {
            Napier.w { "Permission observer is null for observation type ${observationType.observationType}" }
        }
        permissionObserver?.requestPermission(observationType)
    }

    open fun hasPermission(): PermissionApprovalState {
        return permissionObserver?.permissionState(observationType) ?: run {
            Napier.w { "Permission observer is null for observation type ${observationType.observationType}" }
            PermissionApprovalState.NOT_SET
        }
    }

    fun observationConfig(settings: Map<String, Any>) {
        this.lastCollectionTimestamp = (settings[CONFIG_LAST_COLLECTION_TIMESTAMP] as? Long)?.let {
            Instant.fromEpochMilliseconds(it)
        } ?: Clock.System.now()
        if (settings.isNotEmpty()) {
            Napier.i(tag = "Observation::observationConfig") { "Applying new observation settings for ${observationType.observationType}: $settings" }
            val newConfig = this.config + settings
            if (newConfig != this.config) {
                configChanged = true
                this.config += newConfig
            }
        }
    }

    protected fun observationTypeFor(observationId: String): String? =
        observationTypes[observationId]

    protected fun collectionTimestampToNow() {
        Napier.d(tag = "Observation::collectionTimeStampToNow") { "Collecting timestamp" }
        lastCollectionTimestamp = Clock.System.now()
        StudyScope.launch(Dispatchers.IO) {
            lastCollectionTimestamp?.let {
                repos.observation.updateLastCollection(
                    observationIds.toSet(),
                    it.toEpochMilliseconds()
                )
            }
        }
    }

    /**
     * Registers every schedule of this observation type whose window overlaps
     * `[lastCollectionTimestamp, now)` - i.e. currently active/running ones plus already-completed
     * ones that were still active after the last collection - so [storeData]/[storeInstant] tag
     * data with the right observationId/scheduleId. Needed by background poll runs (e.g. triggered
     * by a [io.redlink.more.observations.polling.PollingTaskScheduler] task/worker while the app
     * wasn't otherwise running) where the normal `start()` lifecycle never populated them.
     */
    protected suspend fun registerRecentSchedules(now: Instant = Clock.System.now()) {
        getLastCollectionTimestamp()?.let { from ->
            val fromEpoch = from.epochSeconds
            val nowEpoch = now.epochSeconds
            repos.schedule.allSchedulesWithStatus(false)
                .firstOrNull().orEmpty()
                .filter { schedule ->
                    observationType.matches(schedule.observationType) &&
                            (schedule.start == null || schedule.start <= nowEpoch) &&
                            (schedule.end == null || schedule.end >= fromEpoch)
                }
                .forEach { schedule ->
                    observationIds.add(schedule.observationId)
                    observationTypes[schedule.observationId] = schedule.observationType
                    scheduleIds[schedule.scheduleId] = schedule.observationId
                }
        }
    }

    protected suspend fun getLastCollectionTimestamp(): Instant? {
        return (lastCollectionTimestamp ?: repos.study.getStudy()
            .firstOrNull()?.start?.let { Instant.fromEpochSeconds(it) })
    }

    protected abstract fun start(): Boolean

    protected open fun stop(onCompletion: () -> Unit) = onCompletion()



    fun observerAccessible(): Boolean {
        val errors = observerErrors()
        Napier.d(tag = "Observation::observerAccessible") { errors.toString() }
        return errors.isEmpty()
    }

    protected open fun observerErrors(): Set<String> = emptySet()

    open suspend fun updateObservationPermissions() =
        permissionQueryMutex.withLock {
            when (hasPermission()) {
                PermissionApprovalState.NOT_SET -> {
                    Napier.w {
                        "Permissions not given for observation " +
                                "${observationType.observationType}! Requesting permissions..."
                    }
                    requestPermission()
                }

                PermissionApprovalState.DECLINED -> {
                    Napier.w {
                        "Permissions declined for observation " +
                                "${observationType.observationType}! " +
                                "Showing missing permission alert..."
                    }
                    showMissingPermissionAlert()
                }

                PermissionApprovalState.GRANTED -> {
                    Napier.d {
                        "All permissions given for observation " +
                                "${observationType.observationType}!"
                    }
                }
            }
        }

    protected suspend fun permissionStates(
        collectors: Collection<PermissionCollector>
    ): Map<String, PermissionApprovalState> =
        collectorPermissionQueryMutex.withLock {
            permissionObserver?.permissionStates(collectors)
                ?: collectors.associate { it.permissionKey to it.permissionState() }
        }

    protected suspend fun requestPermissions(
        collectors: Collection<PermissionCollector>
    ) = collectorPermissionQueryMutex.withLock {
        permissionObserver?.requestPermissions(collectors)
            ?: collectors.forEach { it.requestPermission() }
    }

    /**
     * Informs the user that this observation's permission is missing (declined, not just
     * unrequested) and lets them jump straight to the system settings to grant it - unlike
     * [PermissionApprovalState.NOT_SET], which can be silently re-requested via [requestPermission]'s
     * platform prompt. Subclasses sharing one instance across several sub-permissions (e.g.
     * [io.redlink.more.observations.healthConnect.HealthConnectObservation]) may pass a more
     * specific [titleDesc] naming the affected sub-permission instead of the whole observation type.
     */
    protected fun showMissingPermissionAlert(
        titleDesc: StringDesc = StringDesc.Raw(observationType.observationType)
    ) {
        AlertController.openAlertDialog(
            AlertDialogModel(
                title = SharedRes.strings.observation_permission_missing_title.desc(),
                message = SharedRes.strings.observation_permission_missing_message.formatted(
                    listOf(titleDesc)
                ),
                confirmLabel = SharedRes.strings.goals_reminder_open_settings.desc(),
                cancelLabel = SharedRes.strings.goals_reminder_continue_anyway.desc(),
                onConfirm = { openSystemSettings() }
            )
        )
    }

    suspend fun updateObservationErrors() =
        errorQueryMutex.withLock {
            repos.schedule
                .allSchedulesToday(observationType)
                .firstOrNull()
                ?.let { schedules ->
                    if (schedules.isNotEmpty()) {
                        Napier.d(tag = "Observation::updateObservationErrors") {
                            "ObservationErrors for " +
                                    observationType.observationType
                        }

                        if (repos.study.studyState.value.isActive()) {
                            ObservationStates.updateObservationErrors(
                                observationType.observationType,
                                observerErrors()
                            )
                        }
                    }
                }
        }

    protected abstract fun applyObservationConfig(settings: Map<String, Any>)

    open fun bleDevicesNeeded(): Set<String> = emptySet()

    open fun ableToAutomaticallyStart() = true

    fun <T> storeInstant(data: T, timestamp: Long) {
        longRunningStorage?.storeInstant(data, timestamp)
    }

    fun <T> startLongRunningObservation(data: T, identifier: String, timestamp: Long) {
        longRunningStorage?.startObservation(data, identifier, timestamp)
    }

    fun <T> upsertLongRunningObservation(data: T, identifier: String, timestamp: Long) {
        longRunningStorage?.updateObservation(data, identifier, timestamp)
    }

    fun <T> finishLongRunningObservation(data: T, identifier: String, timestamp: Long) {
        longRunningStorage?.finishObservation(data, identifier, timestamp)
    }

    fun <T> inRangeLongRunningObservation(data: T, identifier: String, timestamp: Long) {
        longRunningStorage?.inRangeObservation(data, identifier, timestamp)
    }

    /**
     * Upserts the single most recent data point for [scheduleId], overwriting whatever was
     * stored for that schedule before - unlike [storeData], which appends to the full history,
     * this only exists to back "current value" visualizations (e.g. today list items).
     */
    protected suspend fun storeLatestDataPoint(
        scheduleId: String,
        observationId: String,
        observationType: String,
        data: Any?,
        timestamp: Long
    ) {
        Napier.d { "Storing new datapoint for scheduleId: $scheduleId; observationId: $observationId, type: $observationType, $data" }
        repos.observation.storeLatestDataPoint(
            LatestObservationDataEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = observationType,
                dataValue = data?.asString() ?: "{}",
                timestamp = timestamp
            )
        )
    }

    fun storeData(data: Map<String, Any>, timestamp: Long = -1, onCompletion: () -> Unit = {}) {
        val dataSchemas = ObservationDataEntity.fromData(
            observationIds.toSet(), setOf(ObservationBulkModel(data, timestamp))
        ).map {
            it.observationType =
                observationTypes[it.observationId] ?: observationType.observationType
            it
        }
        Napier.i(tag = "Observation::storeData") { "Observation, with ids $observationIds, ${observationType.observationType} recorded a new data point!" }
        dataManager?.add(dataSchemas, scheduleIds.keys)
        onCompletion()
    }

    fun storeData(data: List<ObservationBulkModel>, onCompletion: () -> Unit) {
        val dataSchemas = ObservationDataEntity.fromData(observationIds.toSet(), data)
            .map {
                it.observationType =
                    observationTypes[it.observationId] ?: observationType.observationType
                it
            }
        Napier.i(tag = "Observation::storeData") { "Observation, with ids $observationIds, ${observationType.observationType} recorded new datapoints!" }
        dataManager?.add(dataSchemas, scheduleIds.keys)
        onCompletion()
    }

    open fun stopAndFinish(scheduleId: String) {
        Napier.i(tag = "Observation::stopAndFinish") { "Stopping and finishing observation ${observationType.observationType} for scheduleId: $scheduleId" }
        if (scheduleIds.size <= 1) {
            stop {
                timestampCollectionJob?.cancel()
                saveAndSend()
                observationShutdown(scheduleId)
            }
        } else {
            saveAndSend()
            observationShutdown(scheduleId)
        }
        Scope.launch {
            updateObservationErrors()
        }
    }

    // Used in iOS
    fun stopAndSetState(state: ScheduleState = ScheduleState.ACTIVE, scheduleId: String?) {
        Napier.d(tag = "Observation::stopAndSetState") { "Stopping observation of type ${observationType.observationType} and setting state to $state for schedule $scheduleId." }
        if (scheduleIds.size <= 1 || scheduleId == null) {
            stop {
                timestampCollectionJob?.cancel()
                saveAndSend()
                scheduleIds.keys.forEach {
                    StudyScope.launch(Dispatchers.IO) {
                        repos.schedule.setRunningStateFor(it, state)
                    }
                }
                scheduleId?.let {
                    observationShutdown(it)
                }
            }
        } else {
            saveAndSend()
            StudyScope.launch(Dispatchers.IO) {
                repos.schedule.setRunningStateFor(scheduleId, state)
            }
            observationShutdown(scheduleId)
        }
        Scope.launch {
            updateObservationErrors()
        }
    }

    open fun stopAndSetDone(scheduleId: String) {
        Napier.d(tag = "Observation::stopAndSetDone") { "Stopping observation of type ${observationType.observationType} and setting done for schedule $scheduleId." }
        if (scheduleIds.size <= 1) {
            stop {
                timestampCollectionJob?.cancel()
                saveAndSend()
                scheduleIds.keys.forEach {
                    StudyScope.launch(Dispatchers.Default) {
                        repos.schedule.setCompletionStateFor(it, true)
                    }
                }
                observationShutdown(scheduleId)
                removeDataCount()
                handleNotification(scheduleId)
                Scope.launch {
                    updateObservationErrors()
                }
            }
        } else {
            saveAndSend()
            StudyScope.launch(Dispatchers.Default) {
                repos.schedule.setCompletionStateFor(scheduleId, true)
            }
            observationShutdown(scheduleId)
            removeDataCount()
            handleNotification(scheduleId)
            Scope.launch {
                updateObservationErrors()
            }
        }
    }

    open fun store(start: Long = -1, end: Long = -1, onCompletion: () -> Unit) {
        Napier.d(tag = "Observation::store") { "Storing data for observation of type ${observationType.observationType} with start time: $start, end time: $end." }
        dataManager?.store()
        onCompletion()
    }

    private fun observationShutdown(scheduleId: String) {
        val observationId = scheduleIds.remove(scheduleId)
        observationId?.let { id ->
            if (scheduleIds.values.none { it == id }) {
                observationIds.remove(id)
                observationTypes.remove(id)
            }
        }
        if (scheduleIds.isEmpty()) {
            config.clear()
            configChanged = false
            running = false
            deactivate()
        }
    }

    private fun handleNotification(scheduleId: String) {
        Scope.launch {
            notificationIds.remove(scheduleId)?.let {
                notificationManager?.markNotificationAsCompleted(it)
            } ?: run {
                repos.schedule.scheduleWithId(scheduleId).firstOrNull()?.let {
                    if (it.reminder) {
                        notificationManager?.markNotificationAsCompleted(it.reminderId())
                    }
                }
            }
        }
    }

    protected fun showNotification(title: String, notificationBody: String) {
        val notification = NotificationEntity.build(title, notificationBody)
        Napier.d(tag = "Observation::showNotification") { "Showing notification: $notification" }
        notificationManager?.storeAndDisplayNotification(notification, true)
    }

    protected fun showObservationErrorNotification(
        notificationBody: String,
        fallbackTitle: String = "Error"
    ) {
        StudyScope.launch {
            val observations = scheduleIds.keys
                .mapNotNull { repos.schedule.scheduleWithId(it).firstOrNull()?.observationTitle }
                .toSet()
            val title = if (observations.isNotEmpty()) observations.joinToString(
                ", ",
                limit = 5
            ) else fallbackTitle
            withContext(Dispatchers.Main) {
                showNotification(title, notificationBody)
            }
        }
    }

    open fun saveAndSend() {
        Napier.d(tag = "Observation::finish") { "Saving and sending data for observation of type ${observationType.observationType}." }
        dataManager?.store()
    }

    fun isRunning() = running

    fun removeDataCount() {
        Napier.d(tag = "Observation::removeDataCount") { "Removing data point count for observation of type ${observationType.observationType}." }
        scheduleIds.keys.forEach {
            dataManager?.removeDataPointCount(it)
        }
        scheduleIds.clear()
    }

    open fun onStudyExit() {
        deactivate()
    }

    /**
     * Activates the shared background poll request (WorkManager `Worker` on Android,
     * `BGAppRefreshTask` on iOS) for this observation's [pollIntervalMillis], called once the
     * observation is stored and running in an active study. Observations that don't need
     * background polling simply leave [pollIntervalMillis] `null`. Safe to call repeatedly -
     * [PollingObservationRegistry] never resubmits an already-active request.
     */
    open fun activate() {
        pollIntervalMillis()?.let { interval ->
            PollingObservationRegistry.activate(observationType.observationType, interval)
        }
    }

    /**
     * Deactivates the background poll request started by [activate] - called on study exit, when
     * the last schedule referencing this observation is paused, and on observation completion.
     */
    open fun deactivate() {
        if (pollIntervalMillis() != null) {
            PollingObservationRegistry.deactivate(observationType.observationType)
        }
    }

    /** Ideal background poll interval for this observation, or `null` if it doesn't poll in the background. */
    protected open fun pollIntervalMillis(): Long? = null

    companion object {
        private val requestedPermissions = mutableSetOf<String>()

        fun resetRequestedPermissions() {
            Napier.d(tag = "Observation::companion") { "Resetting requested permissions" }
            requestedPermissions.clear()
        }

        fun markPermissionRequested(observationType: String) {
            requestedPermissions.add(observationType)
        }

        fun isPermissionRequested(observationType: String): Boolean {
            return requestedPermissions.contains(observationType)
        }

        const val CONFIG_TASK_START = "observation_start_date_time"
        const val CONFIG_TASK_STOP = "observation_stop_date_time"
        const val SCHEDULE_ID = "schedule_id"
        const val CONFIG_LAST_COLLECTION_TIMESTAMP = "observation_last_collection_timestamp"

        const val ERROR_DEVICE_NOT_CONNECTED = "error_device_not_connected"

        /**
         * Computes the `[from, to)` collection window, bounded below by the later of the last
         * collection timestamp and the task/schedule start, and above by the earlier of now and
         * the task/schedule stop. Returns `null` when the window is empty (nothing to collect).
         */
        internal fun computeWindow(
            lastCollectionTimestamp: Instant,
            taskStart: Instant?,
            taskStop: Instant?,
            now: Instant
        ): Pair<Instant, Instant>? {
            val from = maxOf(lastCollectionTimestamp, taskStart ?: lastCollectionTimestamp)
            val to = taskStop?.let { minOf(it, now) } ?: now
            return if (from < to) from to to else null
        }
    }
}
