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
package io.redlink.more.viewModels.tasks

import io.redlink.more.database.entities.DataPointEntity
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.mocks.MockDataPointCountRepository
import io.redlink.more.mocks.MockDataRecorder
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.observations.Observation
import io.redlink.more.observations.ObservationStates
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoreTaskDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepo: MockMainRepository
    private lateinit var mockDataRecorder: MockDataRecorder
    private lateinit var viewModel: CoreTaskDetailsViewModel
    private val scheduleId = "s1"
    private val observationId = "obs1"

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        AppDispatchers.set(default = testDispatcher, main = testDispatcher, io = testDispatcher)
        mockRepo = MockMainRepository()
        mockDataRecorder = MockDataRecorder()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        AppDispatchers.reset()
        ObservationStates.resetAll()
    }

    @Test
    fun testInitializationLoadsTaskDetails() = runTest {
        val observation = ObservationEntity(
            observationId = observationId,
            observationTitle = "Task Title",
            observationType = "TaskType",
            participantInfo = "Participant Information"
        )
        val schedule = ScheduleEntity(
            scheduleId = scheduleId,
            observationId = observationId,
            start = 1000L,
            end = 2000L
        )

        mockRepo.mockObservation.storeObservation(observation)
        mockRepo.mockSchedule.storeSchedule(schedule)

        viewModel = CoreTaskDetailsViewModel(mockRepo, mockDataRecorder, scheduleId)
        runCurrent()

        val details = viewModel.taskDetailsModel.value
        assertNotNull(details, "TaskDetailsModel should not be null")
        assertEquals("Task Title", details.observationTitle)
        assertEquals("TaskType", details.observationType)
    }

    @Test
    fun testDataCountUpdates() = runTest {
        val dataPoint = DataPointEntity(scheduleId = scheduleId, count = 42L)
        (mockRepo.mockDataPointCount as MockDataPointCountRepository).dataPointResults[scheduleId] =
            flowOf(dataPoint)

        viewModel = CoreTaskDetailsViewModel(mockRepo, mockDataRecorder, scheduleId)
        runCurrent()

        assertEquals(42L, viewModel.dataCount.value)
    }

    @Test
    fun testObservationErrorsHandling() = runTest {
        val observation = ObservationEntity(
            observationId = observationId,
            observationType = "TaskType"
        )
        val schedule = ScheduleEntity(
            scheduleId = scheduleId,
            observationId = observationId,
            start = 1000L,
            end = 2000L
        )
        mockRepo.mockObservation.storeObservation(observation)
        mockRepo.mockSchedule.storeSchedule(schedule)

        ObservationStates.updateObservationErrors(
            "TaskType",
            setOf(Observation.ERROR_DEVICE_NOT_CONNECTED, "Other Error")
        )

        viewModel = CoreTaskDetailsViewModel(mockRepo, mockDataRecorder, scheduleId)
        runCurrent()

        assertEquals(1, viewModel.taskObservationErrorActions.value.size)
        assertEquals(
            Observation.ERROR_DEVICE_NOT_CONNECTED,
            viewModel.taskObservationErrorActions.value[0]
        )
        assertEquals(1, viewModel.taskObservationErrors.value.size)
        assertEquals("Other Error", viewModel.taskObservationErrors.value[0])
    }

    @Test
    fun testObservationActions() = runTest {
        viewModel = CoreTaskDetailsViewModel(mockRepo, mockDataRecorder, scheduleId)
        runCurrent()
        viewModel.startObservation()
        assertTrue(mockDataRecorder.startCalled)

        viewModel.pauseObservation()
        assertTrue(mockDataRecorder.pauseCalled)

        viewModel.stopObservation()
        assertTrue(mockDataRecorder.stopCalled)
    }
}
