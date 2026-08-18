package io.redlink.more.observations

import io.redlink.more.SharedRes
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.dialog.AlertController
import io.redlink.more.extensions.desc
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockObservationFactory
import io.redlink.more.mocks.MockStudyMoreScope
import io.redlink.more.models.ScheduleState
import io.redlink.more.observations.observationTypes.ObservationType
import io.redlink.more.scopes.MoreDispatchers
import io.redlink.more.services.store.PermissionApprovalState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ObservationManagerTest {
    private lateinit var repository: MockMainRepository
    private lateinit var observationFactory: MockObservationFactory
    private lateinit var dataRecorder: MockDataRecorder
    private lateinit var observationManager: ObservationManager
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    private val testDispatchers = object : MoreDispatchers {
        override val default: CoroutineDispatcher get() = testDispatcher
        override val main: CoroutineDispatcher get() = testDispatcher
        override val io: CoroutineDispatcher get() = testDispatcher
    }

    @BeforeTest
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
        repository = MockMainRepository()
        observationFactory = MockObservationFactory(repository)
        dataRecorder = MockDataRecorder()
        observationManager = ObservationManager(
            repository,
            observationFactory,
            dataRecorder,
            MockStudyMoreScope(testScope),
            testDispatchers
        )
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
        Observation.resetRequestedPermissions()
    }

    @Test
    fun testStartSchedule() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)

        val result = observationManager.start(scheduleId)

        assertTrue(result)
        assertTrue(mockObservation.startCalled)
        assertEquals(scheduleId, mockObservation.lastScheduleId)
        advanceUntilIdle()
        assertEquals(ScheduleState.RUNNING, repository.mockSchedule.lastSetRunningState?.second)
    }

    @Test
    fun testStartAlreadyRunning() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)

        observationManager.start(scheduleId)
        val result = observationManager.start(scheduleId)

        assertFalse(result)
    }

    @Test
    fun testPauseSchedule() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)
        observationManager.start(scheduleId)

        observationManager.pause(scheduleId)

        assertTrue(mockObservation.stopCalled)
        advanceUntilIdle()
        assertEquals(ScheduleState.PAUSED, repository.mockSchedule.lastSetRunningState?.second)
    }

    @Test
    fun testStopSchedule() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)
        observationManager.start(scheduleId)

        observationManager.stop(scheduleId)

        assertTrue(mockObservation.stopCalled)
        advanceUntilIdle()
        assertEquals(scheduleId, repository.mockSchedule.lastSetCompletionState?.first)
        assertEquals(repository.mockSchedule.lastSetCompletionState?.second, true)
    }

    @Test
    fun testPauseObservationType() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)
        observationManager.start(scheduleId)

        observationManager.pauseObservationType(type)

        assertTrue(mockObservation.stopCalled)
        advanceUntilIdle()
        assertEquals(ScheduleState.PAUSED, repository.mockSchedule.lastSetRunningState?.second)
    }

    @Test
    fun testStartObservationType() = runTest(testDispatcher) {
        val type = "test_type"
        val scheduleId = "s1"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationType = type,
                state = ScheduleState.ACTIVE.name
            )
        )
        observationFactory.matchingObservationTypes = setOf(type)

        observationManager.startObservationType(type)

        assertEquals(setOf(scheduleId), dataRecorder.lastStartedMultipleScheduleIds)
    }

    @Test
    fun testRestartStillRunning() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type,
                state = ScheduleState.RUNNING.name
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)

        val started = observationManager.restartStillRunning()

        assertTrue(scheduleId in started)
        assertTrue(mockObservation.startCalled)
    }

    @Test
    fun testStopAll() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)
        observationManager.start(scheduleId)

        observationManager.stopAll()

        assertTrue(mockObservation.stopAndFinishCalled)
        advanceUntilIdle()
        assertTrue(repository.mockSchedule.lastSetCompletionState?.second == true)
    }

    @Test
    fun testCollectAllData() = runTest(testDispatcher) {
        val scheduleId = "s1"
        val observationId = "o1"
        val type = "test_type"
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                scheduleId = scheduleId,
                observationId = observationId,
                observationType = type
            )
        )
        repository.mockObservation.storeObservation(
            ObservationEntity(observationId = observationId, observationType = type)
        )
        val mockObservation = MockObservation(repository, ObservationType(type, emptySet()))
        observationFactory.observations.add(mockObservation)
        observationManager.start(scheduleId)

        observationManager.upToDateTimestamps = mapOf(type to 1000L)

        var completed = false
        observationManager.collectAllData {
            completed = it
        }

        advanceUntilIdle()
        assertTrue(completed)
        assertTrue(mockObservation.storeCalled)
        assertEquals(1000L, mockObservation.lastStoreStart)
    }

    @Test
    fun testUpdateObservationPermissionsRequestsPermissionWhenNotSet() = runTest {
        val mockObservation =
            MockObservation(repository, ObservationType("simple-observation", emptySet()))
        val permissionObserver = FakePermissionObserver(PermissionApprovalState.NOT_SET)
        mockObservation.setPermissionObserver(permissionObserver)

        mockObservation.updateObservationPermissions()

        assertEquals(1, permissionObserver.requestPermissionCallCount)
    }

    @Test
    fun testUpdateObservationPermissionsShowsAlertWhenDeclined() = runTest {
        val mockObservation =
            MockObservation(repository, ObservationType("simple-observation", emptySet()))
        val permissionObserver = FakePermissionObserver(PermissionApprovalState.DECLINED)
        mockObservation.setPermissionObserver(permissionObserver)

        mockObservation.updateObservationPermissions()

        assertEquals(
            SharedRes.strings.observation_permission_missing_title.desc(),
            AlertController.alertDialogModel.value?.title
        )
        assertEquals(0, permissionObserver.requestPermissionCallCount)
        AlertController.closeAlertDialog()
    }

    @Test
    fun testUpdateObservationPermissionsDoesNothingWhenGranted() = runTest {
        val mockObservation =
            MockObservation(repository, ObservationType("simple-observation", emptySet()))
        val permissionObserver = FakePermissionObserver(PermissionApprovalState.GRANTED)
        mockObservation.setPermissionObserver(permissionObserver)
        val alertBefore = AlertController.alertDialogModel.value

        mockObservation.updateObservationPermissions()

        assertEquals(0, permissionObserver.requestPermissionCallCount)
        assertEquals(alertBefore, AlertController.alertDialogModel.value)
    }

    class FakePermissionObserver(private val state: PermissionApprovalState) :
        ObservationPermissionObserver {
        var requestPermissionCallCount = 0

        override fun requestPermission(observationType: ObservationType) {
            requestPermissionCallCount++
        }

        override fun permissionState(observationType: ObservationType): PermissionApprovalState = state
    }

    class MockDataRecorder : DataRecorder {
        var lastStartedScheduleId: String? = null
        var lastStartedMultipleScheduleIds: Set<String>? = null
        var lastPausedScheduleId: String? = null
        var lastStoppedScheduleId: String? = null
        var stopAllCalled = false
        var restartAllCalled = false

        override fun start(scheduleId: String) {
            lastStartedScheduleId = scheduleId
        }

        override fun startMultiple(scheduleIds: Set<String>) {
            lastStartedMultipleScheduleIds = scheduleIds
        }

        override fun pause(scheduleId: String) {
            lastPausedScheduleId = scheduleId
        }

        override fun stop(scheduleId: String) {
            lastStoppedScheduleId = scheduleId
        }

        override fun stopAll() {
            stopAllCalled = true
        }

        override fun restartAll() {
            restartAllCalled = true
        }
    }

    class MockObservation(repos: MainRepository, observationType: ObservationType) :
        Observation(repos, observationType) {
        var startCalled = false
        var lastScheduleId: String? = null
        var stopCalled = false
        var stopAndFinishCalled = false
        var storeCalled = false
        var lastStoreStart: Long = -1

        override fun start(): Boolean = true

        override fun stop(onCompletion: () -> Unit) {
            onCompletion()
        }

        override fun applyObservationConfig(settings: Map<String, Any>) {
            //
        }

        override suspend fun start(
            observationId: String,
            scheduleId: String,
            notificationId: String?
        ): Boolean {
            startCalled = true
            lastScheduleId = scheduleId
            return true
        }

        override fun stop(scheduleId: String, removeNotification: Boolean) {
            stopCalled = true
        }

        override fun stopAndFinish(scheduleId: String) {
            stopAndFinishCalled = true
        }

        override fun store(start: Long, end: Long, onCompletion: () -> Unit) {
            storeCalled = true
            lastStoreStart = start
            onCompletion()
        }
    }
}
