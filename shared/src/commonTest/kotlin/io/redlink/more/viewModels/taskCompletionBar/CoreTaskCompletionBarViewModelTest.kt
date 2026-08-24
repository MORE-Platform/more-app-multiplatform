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
package io.redlink.more.viewModels.taskCompletionBar

import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CoreTaskCompletionBarViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepo: MockMainRepository
    private lateinit var mockScheduleRepo: MockScheduleRepository
    private lateinit var viewModel: CoreTaskCompletionBarViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepo = MockMainRepository()
        mockScheduleRepo = mockRepo.schedule as MockScheduleRepository
    }

    private fun createViewModel() {
        viewModel = CoreTaskCompletionBarViewModel(mockRepo, testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest {
        createViewModel()
        advanceUntilIdle()
        val taskCompletion = viewModel.taskCompletion.value
        assertEquals(0, taskCompletion.finishedTasks)
        assertEquals(0, taskCompletion.totalTasks)
    }

    @Test
    fun testTaskCompletionUpdates() = runTest {
        val s1 = ScheduleEntity(scheduleId = "1", done = false)
        val s2 = ScheduleEntity(scheduleId = "2", done = true)
        val s3 = ScheduleEntity(scheduleId = "3", done = true)

        mockScheduleRepo.storeSchedule(s1)
        mockScheduleRepo.storeSchedule(s2)
        mockScheduleRepo.storeSchedule(s3)

        createViewModel()
        advanceUntilIdle()

        val taskCompletion = viewModel.taskCompletion.value
        assertEquals(2, taskCompletion.finishedTasks)
        assertEquals(3, taskCompletion.totalTasks)
    }

    @Test
    fun testTaskCompletionChanges() = runTest {
        val s1 = ScheduleEntity(scheduleId = "1", done = false)
        mockScheduleRepo.storeSchedule(s1)

        createViewModel()
        advanceUntilIdle()
        assertEquals(1, viewModel.taskCompletion.value.totalTasks)
        assertEquals(0, viewModel.taskCompletion.value.finishedTasks)

        mockScheduleRepo.setCompletionStateFor("1", true)
        advanceUntilIdle()
        assertEquals(1, viewModel.taskCompletion.value.totalTasks)
        assertEquals(1, viewModel.taskCompletion.value.finishedTasks)
    }
}
