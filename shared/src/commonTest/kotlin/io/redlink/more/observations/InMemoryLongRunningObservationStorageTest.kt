/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.observations

import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.observations.longRunningObservation.InMemoryLongRunningObservationStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemoryLongRunningObservationStorageTest {

    @Test
    fun testStoreInstant() {
        var storedData: Any? = null
        var storedTimestamp: Long? = null
        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { data, timestamp ->
                storedData = data
                storedTimestamp = timestamp
            },
            onFinish = { _, _, _, _ -> }
        )

        val data = "instantData"
        val timestamp = 123456789L
        storage.storeInstant(data, timestamp)

        assertEquals(data, storedData)
        assertEquals(timestamp, storedTimestamp)
    }

    @Test
    fun testStartAndFinishObservation() {
        var finishedData: Any? = null
        var finishedIdentifier: String? = null
        var finishedStartTimestamp: Long? = null
        var finishedEndTimestamp: Long? = null

        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { _, _ -> },
            onFinish = { data, identifier, startTimestamp, endTimestamp ->
                finishedData = data
                finishedIdentifier = identifier
                finishedStartTimestamp = startTimestamp
                finishedEndTimestamp = endTimestamp
            }
        )

        val startData = "startData"
        val identifier = "obs1"
        val startTimestamp = 1000L
        val endTimestamp = 2000L

        storage.startObservation(startData, identifier, startTimestamp)
        assertTrue(storage.hasOpenObservation(identifier))

        storage.finishObservation(startData, identifier, endTimestamp)
        assertFalse(storage.hasOpenObservation(identifier))

        assertTrue(finishedData is List<*>)
        val finishedList = finishedData as List<*>
        assertEquals(2, finishedList.size)
        val first = finishedList[0] as Pair<*, *>
        val second = finishedList[1] as Pair<*, *>
        assertEquals(startData, first.first)
        assertEquals(startTimestamp, first.second)
        assertEquals(startData, second.first)
        assertEquals(endTimestamp, second.second)
        assertEquals(identifier, finishedIdentifier)
        assertEquals(startTimestamp, finishedStartTimestamp)
        assertEquals(endTimestamp, finishedEndTimestamp)
    }

    @Test
    fun testUpdateObservation() {
        var finishedData: Any? = null
        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { _, _ -> },
            onFinish = { data, _, _, _ ->
                finishedData = data
            }
        )

        val startData = "startData"
        val updateData = "updateData"
        val identifier = "obs1"
        val startTimestamp = 1000L
        val updateTimestamp = 1500L
        val endTimestamp = 2000L

        storage.startObservation(startData, identifier, startTimestamp)
        storage.updateObservation(updateData, identifier, updateTimestamp)
        storage.finishObservation(updateData, identifier, endTimestamp)

        assertTrue(finishedData is List<*>)
        val finishedList = finishedData as List<*>
        assertEquals(3, finishedList.size)
        val first = finishedList[0] as Pair<*, *>
        val second = finishedList[1] as Pair<*, *>
        val third = finishedList[2] as Pair<*, *>
        assertEquals(startData, first.first)
        assertEquals(startTimestamp, first.second)
        assertEquals(updateData, second.first)
        assertEquals(updateTimestamp, second.second)
        assertEquals(updateData, third.first)
        assertEquals(endTimestamp, third.second)
    }

    @Test
    fun testFlush() {
        val finishedObservations = mutableListOf<String>()
        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { _, _ -> },
            onFinish = { _, identifier, _, _ ->
                finishedObservations.add(identifier)
            }
        )

        storage.startObservation("data1", "obs1", 1000L)
        storage.startObservation("data2", "obs2", 1100L)

        assertTrue(storage.hasOpenObservation("obs1"))
        assertTrue(storage.hasOpenObservation("obs2"))

        storage.flush(2000L)

        assertFalse(storage.hasOpenObservation("obs1"))
        assertFalse(storage.hasOpenObservation("obs2"))
        assertEquals(2, finishedObservations.size)
        assertTrue(finishedObservations.contains("obs1"))
        assertTrue(finishedObservations.contains("obs2"))
    }

    @Test
    fun testFinishWithoutExactIdentifier() {
        var finishedIdentifier: String? = null
        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { _, _ -> },
            onFinish = { _, identifier, _, _ ->
                finishedIdentifier = identifier
            }
        )

        storage.startObservation("data1", "obs1", 1000L)
        storage.finishObservation("data1", "somethingElse", 2000L)

        assertEquals("obs1", finishedIdentifier)
        assertFalse(storage.hasOpenObservation("obs1"))
    }

    @Test
    fun testFinishWithNullIdentifier() {
        var finishedIdentifier: String? = null
        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { _, _ -> },
            onFinish = { _, identifier, _, _ ->
                finishedIdentifier = identifier
            }
        )

        storage.startObservation("data1", "obs1", 1000L)
        storage.finishObservation("data1", "obs1", 2000L)

        assertEquals("obs1", finishedIdentifier)
    }

    @Test
    fun testLogEventFamilyMatching() {
        var finishedIdentifier: String? = null
        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { _, _ -> },
            onFinish = { _, identifier, _, _ ->
                finishedIdentifier = identifier
            }
        )

        // APP_IN_FOREGROUND and APP_IN_BACKGROUND share the same family APP_VISIBILITY
        val startEvent = LogEvent.APP_IN_FOREGROUND
        val endEvent = LogEvent.APP_IN_BACKGROUND
        val identifier = "test_identifier"
        val startTimestamp = 1000L
        val endTimestamp = 2000L

        storage.startObservation(startEvent, identifier, startTimestamp)
        storage.finishObservation(endEvent, identifier, endTimestamp)

        assertEquals(identifier, finishedIdentifier)
        assertFalse(storage.hasOpenObservation(identifier))
    }

    @Test
    fun testClear() {
        var finishedCalled = false
        val storage = InMemoryLongRunningObservationStorage(
            onStoreInstant = { _, _ -> },
            onFinish = { _, _, _, _ ->
                finishedCalled = true
            }
        )

        storage.startObservation("data1", "obs1", 1000L)
        assertTrue(storage.hasOpenObservation("obs1"))

        storage.clear()

        assertFalse(storage.hasOpenObservation("obs1"))
        assertFalse(finishedCalled)
    }
}
