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
package io.redlink.more.viewModels.notifications

import io.redlink.more.Shared
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.mocks.InMemoryStorageRepository
import io.redlink.more.mocks.MockBluetoothConnector
import io.redlink.more.mocks.MockDataRecorder
import io.redlink.more.mocks.MockLocalNotificationListener
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockNetworkService
import io.redlink.more.mocks.MockNotificationManager
import io.redlink.more.mocks.MockObservationFactory
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.models.NotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoreNotificationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockFilterViewModel: MockFilterViewModel
    private lateinit var mockNotificationManager: MockNotificationManager
    private lateinit var viewModel: CoreNotificationViewModel

    class MockFilterViewModel : CoreNotificationFilterViewModel() {
        var applyFilterCalled = false
        override fun applyFilter(notificationList: List<NotificationModel>): List<NotificationModel> {
            applyFilterCalled = true
            return notificationList
        }
    }


    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val mockRepo = MockMainRepository()

        mockFilterViewModel = MockFilterViewModel()

        val mockShared = createMockShared(mockRepo)
        mockNotificationManager = MockNotificationManager(
            mockRepo,
            MockLocalNotificationListener(),
            MockNetworkService(),
            mockShared.deeplinkManager,
            mockShared.sharedStorageRepository
        )

        viewModel = CoreNotificationViewModel(mockFilterViewModel, mockNotificationManager)
    }

    private fun createMockShared(repository: MainRepository): Shared {
        return object : Shared(
            localNotificationListener = MockLocalNotificationListener(),
            repositories = repository,
            sharedStorageRepository = InMemoryStorageRepository(),
            observationDataManager = mockObservationDataManager(),
            mainBluetoothConnector = MockBluetoothConnector(),
            observationFactory = MockObservationFactory(repository),
            dataRecorder = MockDataRecorder(),
            connectionStatusFlow = flowOf(true)
        ) {
            // Overriding localNotificationListener if it was open, but it's not.
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialization() = runTest {
        // Just verify it doesn't crash on init
        assertTrue(viewModel.notificationList.value.isEmpty())
    }

    @Test
    fun testHandleNotificationAction() = runTest {
        val notification = NotificationModel(
            notificationId = "1",
            channelId = null,
            title = "Test 1",
            notificationBody = "Body",
            timestamp = 0L,
            priority = 1L,
            read = false,
            completed = false,
            userFacing = true,
            deepLink = "link",
            notificationData = emptyMap()
        )

        viewModel.handleNotificationAction(notification) { _, _ -> }

        assertTrue(mockNotificationManager.handleNotificationInteractionCalled)
    }
}
