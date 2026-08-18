package io.redlink.more.observations.polling

import io.redlink.more.mocks.MockSharedStorageRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PollingObservationRegistryTest {

    private class FakeScheduler : PollingTaskScheduler {
        var scheduleCalls = mutableListOf<Long>()
        var cancelCalls = 0

        override fun schedule(intervalMillis: Long) {
            scheduleCalls.add(intervalMillis)
        }

        override fun cancel() {
            cancelCalls++
        }
    }

    @Test
    fun testActivateSchedulesOnce() {
        val scheduler = FakeScheduler()
        PollingObservationRegistry.init(scheduler, MockSharedStorageRepository())

        PollingObservationRegistry.activate("health-connect-observation", 1000L)

        assertEquals(listOf(1000L), scheduler.scheduleCalls)
        assertTrue("health-connect-observation" in PollingObservationRegistry.activeObservationTypes())
    }

    @Test
    fun testActivateWithSameIntervalDoesNotReschedule() {
        val scheduler = FakeScheduler()
        PollingObservationRegistry.init(scheduler, MockSharedStorageRepository())

        PollingObservationRegistry.activate("health-connect-observation", 1000L)
        PollingObservationRegistry.activate("health-connect-observation", 1000L)

        assertEquals(1, scheduler.scheduleCalls.size)
    }

    @Test
    fun testDeactivateCancelsOnceEmpty() {
        val scheduler = FakeScheduler()
        PollingObservationRegistry.init(scheduler, MockSharedStorageRepository())

        PollingObservationRegistry.activate("health-connect-observation", 1000L)
        PollingObservationRegistry.deactivate("health-connect-observation")

        assertEquals(1, scheduler.cancelCalls)
        assertTrue(PollingObservationRegistry.activeObservationTypes().isEmpty())
    }

    @Test
    fun testDeactivateWithoutActivationIsNoop() {
        val scheduler = FakeScheduler()
        PollingObservationRegistry.init(scheduler, MockSharedStorageRepository())

        PollingObservationRegistry.deactivate("health-connect-observation")

        assertEquals(0, scheduler.cancelCalls)
    }

    @Test
    fun testActivationsPersistAndReloadOnInit() {
        val storage = MockSharedStorageRepository()
        PollingObservationRegistry.init(FakeScheduler(), storage)
        PollingObservationRegistry.activate("health-connect-observation", 1000L)

        val reloadedScheduler = FakeScheduler()
        PollingObservationRegistry.init(reloadedScheduler, storage)

        assertEquals(setOf("health-connect-observation"), PollingObservationRegistry.activeObservationTypes())
    }
}
