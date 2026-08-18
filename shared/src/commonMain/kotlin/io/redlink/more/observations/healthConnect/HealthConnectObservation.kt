package io.redlink.more.observations.healthConnect

import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.Napier
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.events.TimedEventBus
import io.redlink.more.extensions.asString
import io.redlink.more.extensions.desc
import io.redlink.more.extensions.localDate
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.observations.Observation
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.healthConnect.model.HealthConnectSample
import io.redlink.more.observations.observers.ManualObserver
import io.redlink.more.scopes.Scope
import io.redlink.more.services.store.PermissionApprovalState
import io.redlink.more.viewModels.ViewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Single shared Observation serving every currently active Health Connect subtype (heart rate,
 * steps, ...). Actual collection/transformation is delegated to the injected [collectors], one per
 * subtype per platform - adding a new subtype requires no change here, only a new
 * [HealthConnectDataType] entry, [io.redlink.more.observations.healthConnect.model.HealthConnectSample]
 * case, and a [HealthConnectCollector] implementation per platform.
 */
class HealthConnectObservation(
    repos: MainRepository,
    private val observationFactory: ObservationFactory? = null,
    private val collectors: List<HealthConnectCollector>
) : Observation(
    repos,
    HealthConnectObservationType()
), ManualObserver {
    private val permissionStates = mutableMapOf<HealthConnectDataType, PermissionApprovalState>()
    private var taskStart: Instant? = null
    private var taskStop: Instant? = null
    private var pollingJob: Job? = null

    constructor(
        repos: MainRepository,
        collectors: List<HealthConnectCollector>
    ) : this(repos, null, collectors)

    init {
        Scope.launch(Dispatchers.Default) {
            observationFactory?.studyObservationTypes?.collect {
                checkRequiredCollectorPermissions()
            }
        }
        Scope.launch(Dispatchers.Default) {
            TimedEventBus.subscribe {
                collectFromActiveCollectors()
            }
        }
        Scope.launch(Dispatchers.Default) {
            ViewManager.appInForeground.collect {
                if (it) {
                    collectFromActiveCollectors()
                }
            }
        }
    }

    private fun activeCollectors(): List<HealthConnectCollector> {
        val studyTypes = observationFactory?.studyObservationTypes?.value ?: emptySet()
        val scheduleTypes = observationIds.mapNotNull { observationTypeFor(it) }.toSet()
        val activeTypes = studyTypes + scheduleTypes
        return collectors.filter { it.dataType.subTypeValue in activeTypes }
    }

    override suspend fun updateObservationPermissions() {
        permissionQueryMutex.withLock {
            checkRequiredCollectorPermissions()
        }
    }

    /**
     * Queries the collectors required by the currently registered observations
     * and returns their current permission states.
     */
    internal suspend fun checkRequiredCollectorPermissions(): Map<HealthConnectDataType, PermissionApprovalState> {
        val active = activeCollectors()
        val states = permissionStates(active)
        val missing = active.filter {
            states[it.permissionKey] == PermissionApprovalState.NOT_SET ||
                    it.hasUnrequestedBonusPermission()
        }
        if (missing.isNotEmpty()) {
            requestPermissions(missing)
        }
        val refreshedStates = permissionStates(active)
        active.forEach { collector ->
            val state = refreshedStates[collector.permissionKey] ?: PermissionApprovalState.NOT_SET
            permissionStates[collector.dataType] = state
            if (state == PermissionApprovalState.DECLINED) {
                showMissingPermissionAlert(collector.dataType.titleStringDesc())
            }
        }
        return active.associate { collector ->
            collector.dataType to (refreshedStates[collector.permissionKey]
                ?: PermissionApprovalState.NOT_SET)
        }
    }

    private fun HealthConnectDataType.titleStringDesc(): StringDesc = when (this) {
        HealthConnectDataType.HEART_RATE -> HealthConnectStrings.heartRateTypeString
        HealthConnectDataType.STEPS -> HealthConnectStrings.stepsTypeString
    }.desc()

    override fun start(): Boolean {
        if (observerAccessible()) {
            Scope.launch(Dispatchers.Default) {
                collectFromActiveCollectors()
            }
            return true
        }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        pollingJob?.cancel()
        pollingJob = null
        onCompletion()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        taskStart = (settings[CONFIG_TASK_START] as? Long)?.let(Instant::fromEpochSeconds)
        taskStop = (settings[CONFIG_TASK_STOP] as? Long)?.let(Instant::fromEpochSeconds)
    }

    override fun pollIntervalMillis(): Long = POLL_INTERVAL_MILLIS

    /**
     * Entry point for a background poll run (WorkManager `Worker`/`BGAppRefreshTask`) that may
     * happen without this observation ever having gone through its normal `start()` lifecycle -
     * first registers the schedules that were active since the last collection, then collects.
     */
    override suspend fun collectAllData() {
        registerRecentSchedules()
        collectFromActiveCollectors()
    }

    override fun observerErrors(): Set<String> {
        return activeCollectors().mapNotNullTo(mutableSetOf()) { collector ->
            if (permissionStates[collector.dataType] != PermissionApprovalState.GRANTED) {
                "error_health_connect_${collector.dataType.name.lowercase()}_permission"
            } else null
        }
    }

    internal suspend fun collectFromActiveCollectors() {
        val schedules =
            scheduleIds.keys.mapNotNull { repos.schedule.scheduleWithId(it).firstOrNull() }
        val now = Clock.System.now()
        val collectorTimeframes = schedules.mapNotNull { schedule ->
            val start = schedule.start?.let { Instant.fromEpochSeconds(it) } ?: taskStart
            val end = schedule.end?.let { Instant.fromEpochSeconds(it) } ?: taskStop
            val dataType = HealthConnectDataType.fromObservationType(schedule.observationType)
            // Aggregating subtypes (steps) always re-sum the whole day rather than picking up
            // where the last poll left off - HealthKit/Health Connect only return samples
            // *starting* inside the query window, so an incremental window would silently drop
            // any interval straddling the boundary. Re-summing from local midnight is idempotent.
            val lastCollected = if (dataType?.aggregatesDaily == true) {
                now.localDate().atStartOfDayIn(TimeZone.currentSystemDefault())
                    .let { Instant.fromEpochMilliseconds(it.toEpochMilliseconds()) }
            } else {
                repos.observation.latestDataPointTimestamp(schedule.observationType)
                    ?.let { Instant.fromEpochMilliseconds(it) }
                    ?: start
                    ?: getLastCollectionTimestamp()
                    ?: return@mapNotNull null
            }

            computeWindow(
                lastCollected,
                start,
                end,
                now
            )?.let { timeframe ->
                schedule.observationType to timeframe
            }
        }
            .toMap()
            .ifEmpty { return }


        val active = activeCollectors()
        val states = permissionStates(active)
        active.forEach { collector ->
            val permissionState = states[collector.permissionKey] ?: PermissionApprovalState.NOT_SET
            permissionStates[collector.dataType] = permissionState
            if (permissionState != PermissionApprovalState.GRANTED) {
                Napier.w(tag = "HealthConnectObservation") {
                    "Permission not granted for ${collector.dataType}, skipping collection."
                }
                return@forEach
            }
            if (collector.dataType.subTypeValue in collectorTimeframes.keys) {
                collectorTimeframes[collector.dataType.subTypeValue]?.let { (from, to) ->
                    runCatching {
                        if (collector.dataType.aggregatesDaily) {
                            storeAggregatedSample(collector, collector.collect(from, to), from, to)
                        } else {
                            collector.collect(from, to).forEach { sample ->
                                storeSample(collector, sample)
                            }
                        }
                    }.onFailure {
                        Napier.w(throwable = it) { "Failed to collect ${collector.dataType}, skipping." }
                    }
                }
            }
        }
        collectionTimestampToNow()
    }

    /**
     * Stores a sample only for the observationIds matching the collector's own subtype - unlike
     * the generic `Observation.storeData`, which would tag/replicate the data point for every
     * currently active observationId of this shared instance (heart rate + steps alike).
     */
    private suspend fun storeSample(
        collector: HealthConnectCollector,
        sample: HealthConnectSample
    ) {
        Napier.d { "Collected data: $sample" }
        val targetObservationIds =
            observationIds.filter { observationTypeFor(it) == collector.dataType.subTypeValue }
        if (targetObservationIds.isEmpty()) return
        val entities = targetObservationIds.map { id ->
            ObservationDataEntity.fromData(
                sample.transform(),
                sample.timestamp.toEpochMilliseconds()
            )
                .copy(
                    observationId = id,
                    observationType = collector.dataType.subTypeValue,
                )
        }

        Napier.d { "New HC Observations: $entities" }
        dataManager?.add(entities, scheduleIds.keys)

        val rawData = sample.transform()
        scheduleIds.filterValues { it in targetObservationIds }
            .forEach { (scheduleId, observationId) ->
                storeLatestDataPoint(
                    scheduleId = scheduleId,
                    observationId = observationId,
                    observationType = collector.dataType.subTypeValue,
                    data = rawData,
                    timestamp = sample.timestamp.toEpochMilliseconds()
                )
            }
    }

    /**
     * Folds a window's worth of per-interval samples (e.g. HealthKit/Health Connect step
     * records) into a single daily total instead of storing each interval separately - unlike
     * [storeSample], which stores every sample verbatim, this is only used for
     * [HealthConnectDataType.aggregatesDaily] subtypes. Skips the write entirely when the freshly
     * summed total matches what is already stored, so an idle 15-minute poll doesn't enqueue a
     * duplicate upload.
     */
    private suspend fun storeAggregatedSample(
        collector: HealthConnectCollector,
        samples: List<HealthConnectSample>,
        windowStart: Instant,
        windowEnd: Instant
    ) {
        val stepSamples = samples.filterIsInstance<HealthConnectSample.Steps>()
        if (stepSamples.isEmpty()) return

        val targetObservationIds =
            observationIds.filter { observationTypeFor(it) == collector.dataType.subTypeValue }
        if (targetObservationIds.isEmpty()) return

        val stepsGoal = targetObservationIds.firstNotNullOfOrNull {
            repos.observation.getObservationByObservationId(it)?.targetSteps()
        }
        val distanceInMeters = runCatching {
            collector.collectDistanceInMeters(windowStart, windowEnd)
        }.onFailure {
            Napier.w(throwable = it) { "Failed to collect distance for ${collector.dataType}, storing steps without it." }
        }.getOrNull()
        val last = stepSamples.maxBy { it.end }

        val aggregate = HealthConnectSample.Steps(
            timestamp = last.end,
            count = stepSamples.sumOf { it.count },
            start = stepSamples.minOf { it.start },
            end = last.end,
            device = last.device,
            sourceApp = last.sourceApp,
            stepsGoal = stepsGoal,
            distanceInMeters = distanceInMeters
        )

        val rawData = aggregate.transform()
        val encoded = rawData.asString()
        val targetSchedules = scheduleIds.filterValues { it in targetObservationIds }
        val unchanged =
            targetSchedules.keys.isNotEmpty() && targetSchedules.keys.all { scheduleId ->
                repos.observation.latestDataPointForSchedule(scheduleId)
                    .firstOrNull()?.dataValue == encoded
            }
        if (unchanged) return

        Napier.d { "Collected daily aggregate: $aggregate" }
        val entities = targetObservationIds.map { id ->
            ObservationDataEntity.fromData(rawData, aggregate.timestamp.toEpochMilliseconds())
                .copy(
                    observationId = id,
                    observationType = collector.dataType.subTypeValue,
                )
        }
        dataManager?.add(entities, scheduleIds.keys)

        targetSchedules.forEach { (scheduleId, observationId) ->
            storeLatestDataPoint(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = collector.dataType.subTypeValue,
                data = rawData,
                timestamp = aggregate.timestamp.toEpochMilliseconds()
            )
        }
    }

    companion object {
        private const val POLL_INTERVAL_MILLIS = 15 * 60 * 1000L
    }
}

private fun ObservationEntity.targetSteps(): Int? =
    (configAsMap()["targetSteps"] as? JsonPrimitive)?.intOrNull
