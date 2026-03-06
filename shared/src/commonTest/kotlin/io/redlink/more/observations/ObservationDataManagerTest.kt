package io.redlink.more.observations

import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockMoreScope
import io.redlink.more.mocks.MockStudyMoreScope
import io.redlink.more.models.StudyState
import io.redlink.more.scopes.MoreDispatchers
import io.redlink.more.scopes.MoreScope
import io.redlink.more.scopes.StudyMoreScope
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
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ObservationDataManagerTest {
    private lateinit var repository: MockMainRepository
    private lateinit var dataManager: TestObservationDataManager
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
        dataManager = TestObservationDataManager(
            repository,
            MockMoreScope(testScope),
            MockStudyMoreScope(testScope),
            testDispatchers
        )
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun testAddData() = runTest {
        val data = listOf(ObservationDataEntity(dataValue = "test"))
        val scheduleIds = setOf("s1")
        repository.mockStudy.updateStudyState(StudyState.ACTIVE)

        dataManager.add(data, scheduleIds)

        assertEquals(1, repository.mockObservationData.addedData.size)
        assertEquals("test", repository.mockObservationData.addedData[0].dataValue)
        assertEquals(1, repository.mockDataPointCount.increments.size)
        assertEquals(scheduleIds, repository.mockDataPointCount.increments[0].first)
        assertEquals(1L, repository.mockDataPointCount.increments[0].second)
    }

    @Test
    fun testAddEmptyData() = runTest {
        dataManager.add(emptyList(), setOf("s1"))
        assertEquals(0, repository.mockObservationData.addedData.size)
        assertEquals(0, repository.mockDataPointCount.increments.size)
    }

    @Test
    fun testAddDataStudyInactive() = runTest {
        repository.mockStudy.updateStudyState(StudyState.PAUSED)
        val data = listOf(ObservationDataEntity(dataValue = "test"))
        dataManager.add(data, setOf("s1"))

        assertEquals(1, repository.mockObservationData.addedData.size)
        // Should NOT start listening if inactive
        testDispatcher.scheduler.advanceTimeBy(60_000)
        testDispatcher.scheduler.runCurrent()
        assertTrue(!dataManager.sendDataCalled)
    }

    @Test
    fun testRemoveDataPointCount() {
        dataManager.removeDataPointCount("s1")
        // No easy way to verify as scheduleCount is private and removeDataPointCount just removes from it.
        // But we can verify it doesn't crash.
    }

    @Test
    fun testStopListeningToCountChanges() = runTest(testDispatcher) {
        repository.mockStudy.updateStudyState(StudyState.ACTIVE)
        repository.mockObservationData.count = 5
        dataManager.listenToDatapointCountChanges()

        dataManager.stopListeningToCountChanges()

        testDispatcher.scheduler.advanceTimeBy(60_000)
        testDispatcher.scheduler.runCurrent()

        assertTrue(!dataManager.sendDataCalled)
    }

    @Test
    fun testListenToDatapointCountChangesNoConnection() = runTest(testDispatcher) {
        repository.mockObservationData.count = 5
        dataManager.connected = false
        repository.mockStudy.updateStudyState(StudyState.ACTIVE)

        dataManager.listenToDatapointCountChanges()
        testDispatcher.scheduler.advanceTimeBy(60_000)
        testDispatcher.scheduler.runCurrent()

        assertTrue(!dataManager.sendDataCalled)
    }

    @Test
    fun testListenToDatapointCountChangesZeroCount() = runTest(testDispatcher) {
        repository.mockObservationData.count = 0
        dataManager.connected = true
        repository.mockStudy.updateStudyState(StudyState.ACTIVE)

        dataManager.listenToDatapointCountChanges()
        testDispatcher.scheduler.advanceTimeBy(60_000)
        testDispatcher.scheduler.runCurrent()

        assertTrue(!dataManager.sendDataCalled)
    }

    @Test
    fun testSaveAndSend() = runTest(testDispatcher) {
        dataManager.saveAndSend()
        advanceUntilIdle()
        assertTrue(repository.mockObservationData.storeCalled)
    }

    @Test
    fun testStore() = runTest(testDispatcher) {
        dataManager.store()
        advanceUntilIdle()
        assertTrue(repository.mockObservationData.storeCalled)
    }

    @Test
    fun testSendDataSuspend() = runTest(testDispatcher) {
        val result = dataManager.sendData(true)
        assertTrue(result)
        assertTrue(dataManager.sendDataCalled)
        testDispatcher.scheduler.advanceTimeBy(1.seconds)
        assertEquals(true, dataManager.lastImmediately)
    }

    @Test
    fun testListenToDatapointCountChanges() = runTest(testDispatcher) {
        repository.mockObservationData.count = 5
        dataManager.connected = true
        repository.mockStudy.updateStudyState(StudyState.ACTIVE)

        dataManager.listenToDatapointCountChanges()
        testDispatcher.scheduler.advanceTimeBy(60_000)
        testDispatcher.scheduler.runCurrent()

        assertTrue(dataManager.sendDataCalled)
    }

    class TestObservationDataManager(
        repository: MainRepository,
        scope: MoreScope,
        studyScope: StudyMoreScope,
        dispatchers: MoreDispatchers
    ) : ObservationDataManagerImpl(repository, scope, studyScope, dispatchers) {
        var sendDataCalled = false
        var lastImmediately: Boolean? = null

        override fun sendData(immediately: Boolean, onCompletion: (Boolean) -> Unit) {
            sendDataCalled = true
            lastImmediately = immediately
            onCompletion(true)
        }

        var connected = true
        override fun isConnected(): Boolean = connected
    }
}
