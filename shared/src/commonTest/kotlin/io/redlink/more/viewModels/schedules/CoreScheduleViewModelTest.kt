/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.viewModels.schedules

import io.redlink.more.database.repository.MainRepository
import io.redlink.more.mocks.MockDataRecorder
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.models.ScheduleListType
import io.redlink.more.models.ScheduleModel
import io.redlink.more.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoreScheduleViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepo: MockMainRepository
    private lateinit var mockDataRecorder: MockDataRecorder
    private lateinit var mockFilterViewModel: MockFilterViewModel
    private lateinit var viewModel: CoreScheduleViewModel

    class MockFilterViewModel(repository: MainRepository) :
        CoreDashboardFilterViewModel(repository) {
        var applyFilterCalled = false
        override fun applyFilter(scheduleModelList: Collection<ScheduleModel>): Collection<ScheduleModel> {
            applyFilterCalled = true
            return scheduleModelList
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepo = MockMainRepository()

        mockDataRecorder = MockDataRecorder()
        mockFilterViewModel = MockFilterViewModel(mockRepo)

        viewModel = CoreScheduleViewModel(
            mockRepo,
            mockDataRecorder,
            ScheduleListType.ALL,
            mockFilterViewModel
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStartSchedule() = runTest {
        val scheduleId = "s1"
        viewModel.start(scheduleId)
        assertTrue(mockDataRecorder.startCalled)
    }

    @Test
    fun testPauseSchedule() = runTest {
        val scheduleId = "s1"
        viewModel.pause(scheduleId)
        assertTrue(mockDataRecorder.pauseCalled)
    }

    @Test
    fun testStopSchedule() = runTest {
        val scheduleId = "s1"
        viewModel.stop(scheduleId)
        assertTrue(mockDataRecorder.stopCalled)
    }
}
