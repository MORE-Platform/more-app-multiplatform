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

package io.redlink.more.observations.appUsage

import io.github.aakira.napier.Napier
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.logging.EventCollection
import io.redlink.more.logging.EventObserver
import io.redlink.more.observations.Observation
import io.redlink.more.observations.appUsage.model.EventModel
import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.observations.longRunningObservation.InMemoryLongRunningObservationStorage
import io.redlink.more.observations.observationTypes.AppUsageObservationType
import io.redlink.more.services.store.PermissionApprovalState
import io.redlink.more.services.store.PermissionRepository
import io.redlink.more.services.store.PermissionType
import kotlinx.datetime.Clock

class AppUsageObservation(
    repos: MainRepository,
    private val permissionRepository: PermissionRepository
) : Observation(repos, AppUsageObservationType()), EventObserver {

    private sealed class BufferedData {
        data class Instant(val data: EventModel, val timestamp: Long) : BufferedData()
        data class Range(
            val eventKey: String,
            val identifier: String,
            val startTimestamp: Long,
            val endTimestamp: Long,
            val storeWithoutApproval: Boolean = false
        ) : BufferedData()
    }

    private val dataBuffer = mutableListOf<BufferedData>()

    private val storage = InMemoryLongRunningObservationStorage(
        onStoreInstant = { data, timestamp ->
            if (data is EventModel) {
                val logEvent = data.eventType
                if ((trackingApproval == PermissionApprovalState.GRANTED || logEvent.storeWithoutApproval) && observationIds.isNotEmpty() && scheduleIds.isNotEmpty()) {
                    storeImmediate(data, timestamp)
                } else {
                    dataBuffer.add(BufferedData.Instant(data, timestamp))
                    persistBuffer()
                }
            }
        },
        onFinish = { data, identifier, startTimestamp, endTimestamp ->
            val dataList = data as? List<*>
            val firstEventModel = dataList?.firstOrNull() as? Pair<*, *>
            val firstEvent = (firstEventModel?.first as? EventModel)?.eventType
            val eventKey = firstEvent?.family?.key ?: firstEvent?.key ?: "unknown"
            val logEvent = firstEvent
            if ((trackingApproval == PermissionApprovalState.GRANTED || logEvent?.storeWithoutApproval == true) && observationIds.isNotEmpty() && scheduleIds.isNotEmpty()) {
                storeRange(eventKey, identifier, startTimestamp, endTimestamp)
            } else {
                dataBuffer.add(
                    BufferedData.Range(
                        eventKey,
                        identifier,
                        startTimestamp,
                        endTimestamp,
                        logEvent?.storeWithoutApproval == true
                    )
                )
                persistBuffer()
            }
        }
    )
    private var trackingApproval: PermissionApprovalState = PermissionApprovalState.NOT_SET

    init {
        setLongRunningObservationStorage(storage)
        trackingApproval = permissionRepository.getPermission(PermissionType.APP_TRACKING)
        loadBuffer()
        EventCollection.addObserver(this)
    }

    private fun loadBuffer() {
        permissionRepository.loadValue(BUFFER_KEY)?.let { json ->
            try {
                val lines = json.split("\n").filter { it.isNotBlank() }
                lines.forEach { line ->
                    val parts = line.split("|")
                    if (parts.size >= 2) {
                        when (parts[0]) {
                            "INSTANT" -> {
                                if (parts.size >= 5) {
                                    val timestamp = parts[1].toLongOrNull() ?: 0L
                                    val eventType = LogEvent.entries.find { it.key == parts[2] }
                                        ?: LogEvent.OBSERVATION_EVENT
                                    val eventTimestamp = parts[3].toLongOrNull() ?: 0L
                                    val identifier = parts[4].takeIf { it != "null" }
                                    val eventData =
                                        identifier?.let { mapOf("identifier" to it) } ?: emptyMap()
                                    dataBuffer.add(
                                        BufferedData.Instant(
                                            EventModel(eventTimestamp, eventType, eventData),
                                            timestamp
                                        )
                                    )
                                }
                            }

                            "RANGE" -> {
                                if (parts.size >= 6) {
                                    val eventKey = parts[1]
                                    val identifier = parts[2]
                                    val startTimestamp = parts[3].toLongOrNull() ?: 0L
                                    val endTimestamp = parts[4].toLongOrNull() ?: 0L
                                    val storeWithoutApproval = parts[5].toBoolean()
                                    dataBuffer.add(
                                        BufferedData.Range(
                                            eventKey,
                                            identifier,
                                            startTimestamp,
                                            endTimestamp,
                                            storeWithoutApproval
                                        )
                                    )
                                } else if (parts.size >= 5) {
                                    val eventKey = parts[1]
                                    val identifier = parts[2]
                                    val startTimestamp = parts[3].toLongOrNull() ?: 0L
                                    val endTimestamp = parts[4].toLongOrNull() ?: 0L
                                    dataBuffer.add(
                                        BufferedData.Range(
                                            eventKey,
                                            identifier,
                                            startTimestamp,
                                            endTimestamp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Napier.e(tag = "AppUsageObservation::loadBuffer") { "Failed to load buffer: ${e.message}" }
            }
        }
    }

    private fun persistBuffer() {
        val json = dataBuffer.joinToString("\n") {
            when (it) {
                is BufferedData.Instant -> {
                    val identifier = it.data.eventData["identifier"] as? String ?: "null"
                    "INSTANT|${it.timestamp}|${it.data.eventType.key}|${it.data.timestamp}|$identifier"
                }

                is BufferedData.Range -> "RANGE|${it.eventKey}|${it.identifier}|${it.startTimestamp}|${it.endTimestamp}|${it.storeWithoutApproval}"
            }
        }
        permissionRepository.storeValue(BUFFER_KEY, json)
    }

    override fun start(): Boolean {
        flushBufferIfPossible()
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        flushOpenRanges()
        onCompletion()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (hasPermission() == PermissionApprovalState.DECLINED) {
            Napier.e { "App tracking declined!" }
            errors.add("app_usage_tracking_declined")
        }
        return errors
    }

    override fun onStudyExit() {
        (longRunningStorage as? InMemoryLongRunningObservationStorage)?.clear()
        dataBuffer.clear()
        permissionRepository.removeValue(BUFFER_KEY)
    }

    override fun onEvent(
        event: LogEvent,
        message: String?
    ) {
        Napier.d { "New AppUsage Event: $event; Message: $message" }
        if (event == LogEvent.APP_TRACKING_ACCEPTED) {
            if (trackingApproval != PermissionApprovalState.GRANTED) {
                setTrackingApproval(true)
            } else {
                return
            }
        }

        if (event == LogEvent.APP_TRACKING_DECLINED) {
            if (trackingApproval == PermissionApprovalState.GRANTED) {
                flushOpenRanges()
            }
            if (trackingApproval != PermissionApprovalState.DECLINED) {
                setTrackingApproval(false)
            }
        }

        val identifier = message?.takeIf { it.isNotBlank() }
        val endTimestamp = Clock.System.now().toEpochMilliseconds()
        val eventModel = EventModel(
            timestamp = endTimestamp,
            eventType = event,
            eventData = identifier?.let { mapOf("identifier" to it) } ?: emptyMap()
        )

        when {
            event.shouldStoreImmediately() -> {
                storeInstant(
                    eventModel,
                    endTimestamp
                )
            }

            event.isRangeStart() -> {
                startLongRunningObservation(eventModel, identifier ?: event.key, endTimestamp)
            }

            event.inRange() -> {
                val startEvent = event.matchingStartEvent() ?: event
                inRangeLongRunningObservation(
                    eventModel,
                    identifier ?: startEvent.key,
                    endTimestamp
                )
            }

            event.isRangeEnd() -> {
                val startEvent = event.matchingStartEvent() ?: event
                finishLongRunningObservation(eventModel, identifier ?: startEvent.key, endTimestamp)
            }
        }
    }

    private fun flushOpenRanges() {
        val now = Clock.System.now().toEpochMilliseconds()
        (longRunningStorage as? InMemoryLongRunningObservationStorage)?.flush(now)
    }

    private fun setTrackingApproval(approval: Boolean) {
        Napier.i { "Set app tracking approval to: $approval" }
        permissionRepository.updatePermission(PermissionType.APP_TRACKING, approval)
        trackingApproval = permissionRepository.getPermission(PermissionType.APP_TRACKING)
        flushBufferIfPossible()
    }

    private fun flushBufferIfPossible() {
        if (observationIds.isNotEmpty() && scheduleIds.isNotEmpty()) {
            val pendingData = dataBuffer.toList()
            val remaining = mutableListOf<BufferedData>()
            val toStore = mutableListOf<BufferedData>()

            pendingData.forEach {
                val canStore = when (it) {
                    is BufferedData.Instant -> trackingApproval == PermissionApprovalState.GRANTED || it.data.eventType.storeWithoutApproval
                    is BufferedData.Range -> trackingApproval == PermissionApprovalState.GRANTED || it.storeWithoutApproval
                }
                if (canStore) toStore.add(it) else remaining.add(it)
            }

            if (toStore.isNotEmpty()) {
                dataBuffer.clear()
                dataBuffer.addAll(remaining)
                if (dataBuffer.isEmpty()) {
                    permissionRepository.removeValue(BUFFER_KEY)
                } else {
                    persistBuffer()
                }
                toStore.forEach {
                    when (it) {
                        is BufferedData.Instant -> storeImmediate(it.data, it.timestamp)
                        is BufferedData.Range -> storeRange(
                            it.eventKey,
                            it.identifier,
                            it.startTimestamp,
                            it.endTimestamp
                        )
                    }
                }
            }
        }
    }

    private fun storeImmediate(data: EventModel, timestamp: Long) {
        val logEvent = data.eventType
        val eventKey = logEvent.family?.key ?: logEvent.key
        storeData(
            mapOf(
                DATA_KEY to mapOf(
                    "eventKey" to eventKey,
                    "identifier" to (data.eventData["identifier"] as? String ?: logEvent.key),
                    "timestamp" to data.timestamp
                )
            ),
            timestamp / 1000
        ) {}
    }

    private fun storeRange(
        eventKey: String,
        identifier: String,
        startTimestamp: Long,
        endTimestamp: Long
    ) {
        storeData(
            mapOf(
                DATA_KEY to mapOf(
                    "eventKey" to eventKey,
                    "identifier" to identifier,
                    "startTimestamp" to startTimestamp,
                    "endTimestamp" to endTimestamp
                )
            ),
            endTimestamp / 1000
        ) {}
    }


    companion object {
        private const val DATA_KEY = "USER_ACTION"
        private const val BUFFER_KEY = "app_usage_data_buffer"
    }
}

