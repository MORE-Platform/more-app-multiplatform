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
package io.redlink.more.viewModels.observationDetails

import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.scopes.AppDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CoreObservationDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepo: MockMainRepository
    private lateinit var viewModel: CoreObservationDetailsViewModel
    private val observationId = "obs1"

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        AppDispatchers.set(default = testDispatcher, main = testDispatcher, io = testDispatcher)
        mockRepo = MockMainRepository()
        viewModel = CoreObservationDetailsViewModel(mockRepo, observationId)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        AppDispatchers.reset()
    }

    @Test
    fun testViewDidAppearLoadsData() = runTest {
        val observation = ObservationEntity(
            observationId = observationId,
            observationTitle = "Test Observation",
            observationType = "type1",
            participantInfo = "Info"
        )
        val startSchedule = ScheduleEntity(scheduleId = "s1", start = 1000L, end = 2000L)
        val endSchedule = ScheduleEntity(scheduleId = "s2", start = 3000L, end = 4000L)

        mockRepo.mockObservation.storeObservation(observation)
        mockRepo.mockSchedule.firstAndLastDateResult = flowOf(startSchedule to endSchedule)

        viewModel.viewDidAppear()
        runCurrent()

        val model = viewModel.observationDetailsModel.value
        assertNotNull(model, "ObservationDetailsModel should not be null after viewDidAppear")
        assertEquals("Test Observation", model.observationTitle)
        assertEquals(1000L, model.start)
        assertEquals(4000L, model.end)
    }

    @Test
    fun testViewDidDisappearClearsData() = runTest {
        val observation = ObservationEntity(
            observationId = observationId,
            observationTitle = "Test Observation",
            observationType = "type1",
            participantInfo = "Info"
        )
        mockRepo.mockObservation.storeObservation(observation)
        mockRepo.mockSchedule.firstAndLastDateResult = flowOf(null to null)

        viewModel.viewDidAppear()
        runCurrent()
        assertNotNull(viewModel.observationDetailsModel.value)

        viewModel.viewDidDisappear()
        advanceUntilIdle()
        assertNull(viewModel.observationDetailsModel.value)
    }
}
