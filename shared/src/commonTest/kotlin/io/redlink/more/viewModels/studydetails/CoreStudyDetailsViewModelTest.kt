package io.redlink.more.viewModels.studydetails

import io.redlink.more.Shared
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.mocks.InMemoryStorageRepository
import io.redlink.more.mocks.MockBluetoothConnector
import io.redlink.more.mocks.MockDataRecorder
import io.redlink.more.mocks.MockLocalNotificationListener
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockObservationFactory
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.scopes.AppDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class CoreStudyDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepo: MockMainRepository
    private lateinit var viewModel: CoreStudyDetailsViewModel
    private lateinit var shared: Shared

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        AppDispatchers.set(default = testDispatcher, main = testDispatcher, io = testDispatcher)
        mockRepo = MockMainRepository()
        shared = object : Shared(
            localNotificationListener = MockLocalNotificationListener(),
            repositories = mockRepo,
            sharedStorageRepository = InMemoryStorageRepository(),
            observationDataManager = mockObservationDataManager(mockRepo),
            mainBluetoothConnector = MockBluetoothConnector(),
            observationFactory = MockObservationFactory(mockRepo),
            dataRecorder = MockDataRecorder(),
            connectionStatusFlow = flowOf(true)
        ) {}
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        AppDispatchers.reset()
    }

    @Test
    fun testStudyDetailsLoading() = runTest {
        val study = StudyEntity(
            studyTitle = "Study Title",
            consentInfo = "Consent Info",
            participantInfo = "Participant Info"
        )
        val observation = ObservationEntity(
            observationId = "obs1",
            observationTitle = "Observation Title",
            observationType = "type1"
        )
        val schedule = ScheduleEntity(
            scheduleId = "s1",
            observationId = "obs1",
            done = true,
            start = 1000L,
            end = 2000L
        )

        (mockRepo.mockStudy as io.redlink.more.mocks.MockStudyRepository).upsert(study)
        mockRepo.mockObservation.storeObservation(observation)
        mockRepo.mockSchedule.storeSchedule(schedule)

        viewModel = CoreStudyDetailsViewModel(shared)
        runCurrent()

        val model = viewModel.studyModel.value
        assertNotNull(model, "StudyModel should not be null")
        assertEquals("Study Title", model.study.studyTitle)
        assertEquals(1, model.observations.size)
        assertEquals("Observation Title", model.observations[0].observationTitle)
        assertEquals(1, model.totalTasks)
        assertEquals(1, model.finishedTasks)
    }
}
