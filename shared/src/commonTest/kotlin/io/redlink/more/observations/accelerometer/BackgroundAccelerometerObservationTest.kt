package io.redlink.more.observations.accelerometer

import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.ObservationBulkModel
import io.redlink.more.observations.observationTypes.AccelerometerType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class BackgroundAccelerometerObservationTest {

    private val accType = AccelerometerType(emptySet()).observationType

    private class FakeCollector(
        override val isRecordingAvailable: Boolean = true,
        private val samples: List<ObservationBulkModel> = emptyList()
    ) : BackgroundAccelerometerCollector {
        var collectCallCount = 0
        var lastCollectRange: Pair<Instant, Instant>? = null
        val recordedDurations = mutableListOf<Double>()

        override fun record(durationSeconds: Double) {
            recordedDurations.add(durationSeconds)
        }

        override suspend fun collect(from: Instant, to: Instant): List<ObservationBulkModel> {
            collectCallCount++
            lastCollectRange = from to to
            return samples
        }
    }

    @Test
    fun testCollectAllDataCollectsForScheduleActiveSinceStudyStart() = runTest {
        val repository = MockMainRepository()
        val studyStart = Clock.System.now() - 2.hours
        repository.mockStudy.upsert(StudyEntity(start = studyStart.epochSeconds))
        repository.mockObservation.addObservations(
            listOf(ObservationEntity(observationId = "obs-1", observationType = accType))
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    start = studyStart.epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )
        val collector = FakeCollector()
        val observation = BackgroundAccelerometerObservation(repository, emptySet(), collector)
        observation.applyDataManager(mockObservationDataManager(repository))

        observation.collectAllData()

        assertEquals(1, collector.collectCallCount)
        assertEquals(Instant.fromEpochSeconds(studyStart.epochSeconds), collector.lastCollectRange?.first)
    }

    @Test
    fun testComputeRecordDurationDerivesFromTaskWindow() {
        val now = Clock.System.now()
        val duration = BackgroundAccelerometerObservation.computeRecordDuration(
            now, now + 5.hours, now
        )
        assertEquals((5.hours).inWholeSeconds.toDouble(), duration)
    }

    @Test
    fun testComputeRecordDurationFallsBackToDefaultWhenWindowAlreadyElapsed() {
        val now = Clock.System.now()
        val duration = BackgroundAccelerometerObservation.computeRecordDuration(
            now - 2.hours, now - 1.hours, now
        )
        assertEquals(60.0 * 10, duration)
    }

    @Test
    fun testComputeReArmDurationReturnsRemainingWindow() {
        val now = Clock.System.now()
        val duration = BackgroundAccelerometerObservation.computeReArmDuration(now + 3.hours, now)
        assertEquals((3.hours).inWholeSeconds.toDouble(), duration)
    }

    @Test
    fun testComputeReArmDurationReturnsNullWhenWindowIsOver() {
        val now = Clock.System.now()
        assertNull(BackgroundAccelerometerObservation.computeReArmDuration(now - 1.hours, now))
        assertNull(BackgroundAccelerometerObservation.computeReArmDuration(null, now))
    }
}
