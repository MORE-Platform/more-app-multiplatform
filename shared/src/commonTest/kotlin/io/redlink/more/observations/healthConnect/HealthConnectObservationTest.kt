package io.redlink.more.observations.healthConnect

import io.redlink.more.SharedRes
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.dialog.AlertController
import io.redlink.more.extensions.desc
import io.redlink.more.extensions.jsonRead
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockSharedStorageRepository
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.Observation
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.healthConnect.model.HealthConnectSample
import io.redlink.more.observations.polling.PollingObservationRegistry
import io.redlink.more.observations.polling.PollingTaskScheduler
import io.redlink.more.services.store.PermissionApprovalState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class HealthConnectObservationTest {

    private val heartRateType = HealthConnectDataType.HEART_RATE.subTypeValue
    private val stepsType = HealthConnectDataType.STEPS.subTypeValue

    private class FakeCollector(
        override val dataType: HealthConnectDataType,
        private val permission: PermissionApprovalState = PermissionApprovalState.GRANTED,
        private val samples: List<HealthConnectSample> = emptyList(),
        private val distanceInMeters: Double? = null
    ) : HealthConnectCollector {
        var collectCallCount = 0
        var requestPermissionCallCount = 0

        override suspend fun permissionState(): PermissionApprovalState = permission

        override suspend fun requestPermission() {
            requestPermissionCallCount++
        }

        override suspend fun collect(from: Instant, to: Instant): List<HealthConnectSample> {
            collectCallCount++
            return samples
        }

        override suspend fun collectDistanceInMeters(from: Instant, to: Instant): Double? =
            distanceInMeters
    }

    /**
     * [Observation.collectAllData] resolves its collection window from the study's start when no
     * poll has happened yet ([Observation.getLastCollectionTimestamp]) - without this, registerRecentSchedules()
     * silently no-ops and nothing is ever collected.
     */
    private suspend fun MockMainRepository.withStudyStart(): MockMainRepository = apply {
        mockStudy.upsert(StudyEntity(start = (Clock.System.now() - 2.hours).epochSeconds))
    }

    @Test
    fun testSingleInstanceRegisteredForBothSubtypes() {
        val repository = MockMainRepository()
        val factory = object : ObservationFactory(
            repository,
            MockSharedStorageRepository(),
            mockObservationDataManager(repository)
        ) {
            init {
                registerObservation {
                    HealthConnectObservation(
                        repository,
                        listOf(
                            FakeCollector(HealthConnectDataType.HEART_RATE),
                            FakeCollector(HealthConnectDataType.STEPS)
                        )
                    )
                }
            }
        }

        factory.addNeededObservationTypes(setOf(heartRateType, stepsType))

        val healthConnectInstances = factory.observations.filterIsInstance<HealthConnectObservation>()
        assertEquals(1, healthConnectInstances.size)
        assertTrue(
            factory.observationTypes()
                .contains(HealthConnectObservationType().observationType)
        )
    }

    @Test
    fun testNoActiveCollectorsMeansNoCollectorCalls() = runTest {
        val repository = MockMainRepository()
        val heartRateCollector = FakeCollector(HealthConnectDataType.HEART_RATE)
        val stepsCollector = FakeCollector(HealthConnectDataType.STEPS)

        val observation = HealthConnectObservation(repository, listOf(heartRateCollector, stepsCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        observation.collectFromActiveCollectors()

        assertEquals(0, heartRateCollector.collectCallCount)
        assertEquals(0, stepsCollector.collectCallCount)
    }

    @Test
    fun testCheckRequiredCollectorPermissionsShowsAlertOnDeclinedPermission() = runTest {
        val repository = MockMainRepository().withStudyStart()
        val heartRateCollector =
            FakeCollector(HealthConnectDataType.HEART_RATE, permission = PermissionApprovalState.DECLINED)
        val observation = HealthConnectObservation(repository, listOf(heartRateCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        repository.mockObservation.addObservations(
            listOf(ObservationEntity(observationId = "obs-1", observationType = heartRateType))
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    observationType = heartRateType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )
        observation.collectAllData()

        observation.checkRequiredCollectorPermissions()

        assertEquals(
            SharedRes.strings.observation_permission_missing_title.desc(),
            AlertController.alertDialogModel.value?.title
        )
        assertEquals(0, heartRateCollector.requestPermissionCallCount)
        AlertController.closeAlertDialog()
    }

    @Test
    fun testCheckRequiredCollectorPermissionsReturnsCorrectStatePerDataType() = runTest {
        val repository = MockMainRepository().withStudyStart()
        val heartRateCollector =
            FakeCollector(HealthConnectDataType.HEART_RATE, permission = PermissionApprovalState.GRANTED)
        val stepsCollector =
            FakeCollector(HealthConnectDataType.STEPS, permission = PermissionApprovalState.DECLINED)
        val observation = HealthConnectObservation(repository, listOf(heartRateCollector, stepsCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        repository.mockObservation.addObservations(
            listOf(
                ObservationEntity(observationId = "obs-1", observationType = heartRateType),
                ObservationEntity(observationId = "obs-2", observationType = stepsType)
            )
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    observationType = heartRateType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                ),
                ScheduleEntity(
                    scheduleId = "sched-2",
                    observationId = "obs-2",
                    observationType = stepsType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )
        observation.collectAllData()

        val states = observation.checkRequiredCollectorPermissions()

        assertEquals(PermissionApprovalState.GRANTED, states[HealthConnectDataType.HEART_RATE])
        assertEquals(PermissionApprovalState.DECLINED, states[HealthConnectDataType.STEPS])
        assertEquals(2, states.size)
        AlertController.closeAlertDialog()
    }

    @Test
    fun testComputeWindowRespectsLastCollectionAndTaskStart() {
        val now = Clock.System.now()
        val lastCollection = now - 2.hours
        val taskStart = now - 1.hours

        val window = Observation.computeWindow(lastCollection, taskStart, null, now)

        assertEquals(taskStart, window?.first)
        assertEquals(now, window?.second)
    }

    @Test
    fun testComputeWindowRespectsTaskStop() {
        val now = Clock.System.now()
        val lastCollection = now - 2.hours
        val taskStop = now - 1.hours

        val window = Observation.computeWindow(lastCollection, null, taskStop, now)

        assertEquals(lastCollection, window?.first)
        assertEquals(taskStop, window?.second)
    }

    @Test
    fun testComputeWindowReturnsNullWhenLastCollectionIsAfterTaskStop() {
        val now = Clock.System.now()
        val taskStop = now - 2.hours
        val lastCollection = now - 1.hours

        val window = Observation.computeWindow(lastCollection, null, taskStop, now)

        assertNull(window)
    }

    @Test
    fun testCollectAllDataRegistersRecentSchedulesBeforeCollecting() = runTest {
        val repository = MockMainRepository().withStudyStart()
        val heartRateCollector = FakeCollector(HealthConnectDataType.HEART_RATE)
        val observation = HealthConnectObservation(repository, listOf(heartRateCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        repository.mockObservation.addObservations(
            listOf(ObservationEntity(observationId = "obs-1", observationType = heartRateType))
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    observationType = heartRateType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )

        observation.collectAllData()

        assertEquals(1, heartRateCollector.collectCallCount)
    }

    @Test
    fun testActivateAndDeactivateDrivePollingRegistry() {
        val scheduler = object : PollingTaskScheduler {
            var scheduledInterval: Long? = null
            var cancelled = false

            override fun schedule(intervalMillis: Long) {
                scheduledInterval = intervalMillis
            }

            override fun cancel() {
                cancelled = true
            }
        }
        PollingObservationRegistry.init(scheduler, MockSharedStorageRepository())
        val observation = HealthConnectObservation(MockMainRepository(), emptyList<HealthConnectCollector>())

        observation.activate()
        assertEquals(15 * 60 * 1000L, scheduler.scheduledInterval)

        observation.deactivate()
        assertTrue(scheduler.cancelled)
    }

    @Test
    fun testCollectAllDataStoresLatestDataPointForMatchingScheduleOnly() = runTest {
        val repository = MockMainRepository().withStudyStart()
        val now = Clock.System.now()
        val heartRateCollector =
            FakeCollector(HealthConnectDataType.HEART_RATE, samples = listOf(HealthConnectSample.HeartRate(now, 72)))
        val stepsCollector = FakeCollector(HealthConnectDataType.STEPS)
        val observation = HealthConnectObservation(repository, listOf(heartRateCollector, stepsCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        repository.mockObservation.addObservations(
            listOf(ObservationEntity(observationId = "obs-1", observationType = heartRateType))
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    observationType = heartRateType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )

        observation.collectAllData()

        val latest = repository.mockObservation.latestDataPointForSchedule("sched-1").first()
        assertEquals("obs-1", latest?.observationId)
        assertEquals(heartRateType, latest?.observationType)
        val data = latest?.dataValue?.jsonRead<Map<String, Any?>>()
        val payload = data?.get("data") as? Map<*, *>
        assertEquals(72L, payload?.get("hr"))
    }

    @Test
    fun testSampleTransformProducesExpectedPayload() {
        val now = Clock.System.now()
        val heartRate = HealthConnectSample.HeartRate(now, 72)
        val steps = HealthConnectSample.Steps(now, 1000, now - 1.hours, now)

        assertEquals(
            mapOf(
                "timestamp" to now.toString(),
                "data" to mapOf("hr" to 72)
            ),
            heartRate.transform()
        )
        assertEquals(
            mapOf(
                "timestamp" to now.toString(),
                "startTime" to (now - 1.hours).toString(),
                "endTime" to now.toString(),
                "data" to mapOf("steps" to 1000L)
            ),
            steps.transform()
        )
    }

    @Test
    fun testSampleTransformIncludesDeviceAndSourceApp() {
        val now = Clock.System.now()
        val heartRate = HealthConnectSample.HeartRate(
            now,
            72,
            device = "Apple Watch",
            sourceApp = "com.apple.health"
        )

        val transformed = heartRate.transform()

        assertEquals("Apple Watch", transformed["device"])
        assertEquals(mapOf("sourceApp" to "com.apple.health"), transformed["additionalData"])
    }

    @Test
    fun testStepsCollectionAggregatesSamplesIntoOneDailyDataPoint() = runTest {
        val repository = MockMainRepository().withStudyStart()
        val now = Clock.System.now()
        val stepsCollector = FakeCollector(
            HealthConnectDataType.STEPS,
            samples = listOf(
                HealthConnectSample.Steps(now - 30.minutes, 500, now - 1.hours, now - 30.minutes),
                HealthConnectSample.Steps(now, 300, now - 30.minutes, now)
            )
        )
        val observation = HealthConnectObservation(repository, listOf(stepsCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        repository.mockObservation.addObservations(
            listOf(ObservationEntity(observationId = "obs-1", observationType = stepsType))
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    observationType = stepsType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )

        observation.collectAllData()

        val latest = repository.mockObservation.latestDataPointForSchedule("sched-1").first()
        val data = latest?.dataValue?.jsonRead<Map<String, Any?>>()
        val payload = data?.get("data") as? Map<*, *>
        assertEquals(800L, payload?.get("steps"))
    }

    @Test
    fun testStepsCollectionIncludesGoalAndDistanceOnTheAggregate() = runTest {
        val repository = MockMainRepository().withStudyStart()
        val now = Clock.System.now()
        val stepsCollector = FakeCollector(
            HealthConnectDataType.STEPS,
            samples = listOf(HealthConnectSample.Steps(now, 500, now - 1.hours, now)),
            distanceInMeters = 321.5
        )
        val observation = HealthConnectObservation(repository, listOf(stepsCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        repository.mockObservation.addObservations(
            listOf(
                ObservationEntity(
                    observationId = "obs-1",
                    observationType = stepsType,
                    configuration = """{"targetSteps":10000}"""
                )
            )
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    observationType = stepsType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )

        observation.collectAllData()

        val latest = repository.mockObservation.latestDataPointForSchedule("sched-1").first()
        val data = latest?.dataValue?.jsonRead<Map<String, Any?>>()
        val payload = data?.get("data") as? Map<*, *>
        assertEquals(500L, payload?.get("steps"))
        assertEquals(10000L, payload?.get("stepsGoal"))
        assertEquals(321.5, payload?.get("distanceInMeters"))
    }

    @Test
    fun testRepeatedIdenticalStepsCollectionStaysConsistent() = runTest {
        val repository = MockMainRepository().withStudyStart()
        val now = Clock.System.now()
        val stepsCollector = FakeCollector(
            HealthConnectDataType.STEPS,
            samples = listOf(HealthConnectSample.Steps(now, 500, now - 1.hours, now))
        )
        val observation = HealthConnectObservation(repository, listOf(stepsCollector))
        observation.applyDataManager(mockObservationDataManager(repository))

        repository.mockObservation.addObservations(
            listOf(ObservationEntity(observationId = "obs-1", observationType = stepsType))
        )
        repository.mockSchedule.addSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = "sched-1",
                    observationId = "obs-1",
                    observationType = stepsType,
                    start = Clock.System.now().epochSeconds,
                    state = ScheduleState.ACTIVE.name
                )
            )
        )

        observation.collectAllData()
        observation.collectAllData()

        val latest = repository.mockObservation.latestDataPointForSchedule("sched-1").first()
        val data = latest?.dataValue?.jsonRead<Map<String, Any?>>()
        val payload = data?.get("data") as? Map<*, *>
        assertEquals(500L, payload?.get("steps"))
    }
}
