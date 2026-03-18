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

package io.redlink.more.observations.longRunningObservation

/**
 * In-memory implementation of LongRunningObservationStorage.
 * Stores data into a map and provides simple lifecycle management for long-running observations.
 */
class InMemoryLongRunningObservationStorage(
    private val onStoreInstant: (data: Any, timestamp: Long) -> Unit,
    private val onFinish: (data: Any, identifier: String, startTimestamp: Long, endTimestamp: Long) -> Unit
) : LongRunningObservationStorage {

    private val openObservations = mutableMapOf<String, Pair<MutableList<Pair<Any, Long>>, Long>>()

    override fun <T> storeInstant(data: T, timestamp: Long) {
        if (data != null) {
            onStoreInstant(data, timestamp)
        }
    }

    override fun <T> startObservation(data: T, identifier: String, timestamp: Long) {
        if (!openObservations.containsKey(identifier) && data != null) {
            val list: MutableList<Pair<Any, Long>> = mutableListOf((data as Any) to timestamp)
            openObservations[identifier] = list to timestamp
        }
    }

    private fun findOpenKey(identifier: String): String? {
        return if (openObservations.containsKey(identifier)) {
            identifier
        } else {
            openObservations.keys.firstOrNull()
        }
    }

    override fun <T> updateObservation(
        data: T,
        identifier: String,
        timestamp: Long
    ) {
        findOpenKey(identifier)?.let { key ->
            openObservations[key]?.let { (dataList, _) ->
                if (data != null) {
                    dataList.add((data as Any) to timestamp)
                }
            }
        }
    }

    override fun <T> inRangeObservation(data: T, identifier: String, timestamp: Long) {
        updateObservation(data, identifier, timestamp)
    }

    override fun <T> finishObservation(data: T, identifier: String, timestamp: Long) {
        val key = findOpenKey(identifier)
        if (key != null) {
            openObservations.remove(key)?.let { (dataList, startTimestamp) ->
                if (data != null) {
                    dataList.add((data as Any) to timestamp)
                }
                onFinish(dataList, key, startTimestamp, timestamp)
            }
        }
    }

    fun flush(timestamp: Long) {
        val pending = openObservations.toMap()
        openObservations.clear()
        pending.forEach { (identifier, pair) ->
            onFinish(pair.first, identifier, pair.second, timestamp)
        }
    }

    fun clear() {
        openObservations.clear()
    }

    fun hasOpenObservation(identifier: String): Boolean = openObservations.containsKey(identifier)
}
