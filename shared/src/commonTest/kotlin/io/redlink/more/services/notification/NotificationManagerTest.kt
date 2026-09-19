/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.services.notification

import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.mocks.InMemoryStorageRepository
import io.redlink.more.mocks.MockDeeplinkManager
import io.redlink.more.mocks.MockLocalNotificationListener
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockNetworkService
import io.redlink.more.mocks.MockNotificationActionObserver
import io.redlink.more.models.NotificationStatusType
import io.redlink.more.models.StudyState
import io.redlink.more.scopes.AppDispatchers
import io.redlink.more.scopes.MoreDispatchers
import io.redlink.more.services.network.openapi.model.PushNotification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationManagerTest {

    private lateinit var notificationManager: NotificationManager
    private lateinit var repository: MockMainRepository
    private lateinit var localNotificationListener: MockLocalNotificationListener
    private lateinit var networkService: MockNetworkService
    private lateinit var deeplinkManager: MockDeeplinkManager
    private lateinit var sharedStorageRepository: InMemoryStorageRepository
    private lateinit var actionObserver: MockNotificationActionObserver

    private lateinit var testDispatcher: TestDispatcher

    private val testDispatchers = object : MoreDispatchers {
        override val default: CoroutineDispatcher get() = testDispatcher
        override val main: CoroutineDispatcher get() = testDispatcher
        override val io: CoroutineDispatcher get() = testDispatcher
    }

    @BeforeTest
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        AppDispatchers.set(testDispatcher, testDispatcher, testDispatcher)
        repository = MockMainRepository()
        localNotificationListener = MockLocalNotificationListener()
        sharedStorageRepository = InMemoryStorageRepository()
        networkService = MockNetworkService()
        deeplinkManager = MockDeeplinkManager(repository)
        notificationManager = NotificationManager(
            repository,
            localNotificationListener,
            networkService,
            deeplinkManager,
            sharedStorageRepository,
            testDispatchers
        )
        actionObserver = MockNotificationActionObserver()
        notificationManager.setActionObserver(actionObserver)
    }

    @AfterTest
    fun tearDown() {
        AppDispatchers.reset()
        Dispatchers.resetMain()
    }

    @Test
    fun testStoreAndHandleNotification() = runTest(testDispatcher) {
        val key = "test_key"
        val title = "Test Title"
        val body = "Test Body"

        notificationManager.storeAndHandleNotification(
            key = key,
            title = title,
            body = body,
            displayNotification = true
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val storedNotification = repository.notification.getNotification(key)
        assertNotNull(storedNotification)
        assertEquals(title, storedNotification.title)
        assertEquals(body, storedNotification.notificationBody)
        assertEquals(1, localNotificationListener.displayedNotifications.size)
        assertEquals(key, localNotificationListener.displayedNotifications.first().notificationId)
    }

    @Test
    fun testMarkNotificationAsRead() = runTest(testDispatcher) {
        val key = "test_key"
        notificationManager.storeAndHandleNotification(
            key,
            "Title",
            "Body",
            displayNotification = false
        )

        notificationManager.markNotificationAsRead(key)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedNotification = repository.notification.getNotification(key)
        assertNotNull(updatedNotification)
        assertTrue(updatedNotification.read)
    }

    @Test
    fun testMarkNotificationAsCompleted() = runTest(testDispatcher) {
        val key = "test_key"
        notificationManager.storeAndHandleNotification(
            key,
            "Title",
            "Body",
            displayNotification = false
        )

        notificationManager.markNotificationAsCompleted(key)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedNotification = repository.notification.getNotification(key)
        assertNotNull(updatedNotification)
        assertTrue(updatedNotification.completed)
    }

    @Test
    fun testDeleteNotificationFromRepository() = runTest(testDispatcher) {
        val key = "test_key"
        notificationManager.storeAndHandleNotification(
            key,
            "Title",
            "Body",
            displayNotification = false
        )

        notificationManager.deleteNotificationFromRepository(key)
        testDispatcher.scheduler.advanceUntilIdle()

        val deletedNotification = repository.notification.getNotification(key)
        assertEquals(deletedNotification, null)
    }

    @Test
    fun testNewFCMToken() = runTest(testDispatcher) {
        val token = "new_fcm_token"
        notificationManager.newFCMToken(token)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = sharedStorageRepository.load(NotificationManager.FCM_TOKEN_UPLOADED, false)

        assertTrue { result }
    }

    @Test
    fun testScheduleObservationReminders() = runTest(testDispatcher) {
        val schedules = listOf(
            ScheduleEntity(
                scheduleId = "s1",
                observationTitle = "Obs 1",
                start = 1000L,
                reminder = true,
                observationId = "o1"
            ),
            ScheduleEntity(
                scheduleId = "s2",
                observationTitle = "Obs 2",
                start = 2000L,
                reminder = true,
                observationId = "o2"
            )
        )

        notificationManager.scheduleObservationReminders(schedules)
//        delay(50)
        testDispatcher.scheduler.advanceUntilIdle()

        val storedS1 = repository.notification.getNotification("reminder_s1")
        val storedS2 = repository.notification.getNotification("reminder_s2")

        assertNotNull(storedS1)
        assertNotNull(storedS2)
        assertEquals(2, localNotificationListener.displayedNotifications.size)
    }

    @Test
    fun testClearScheduledNotifications() = runTest(testDispatcher) {
        val n1 = NotificationEntity(notificationId = "s1", userFacing = false)
        val n2 = NotificationEntity(notificationId = "s2", userFacing = false)
        repository.notification.storeNotifications(listOf(n1, n2))

        notificationManager.clearScheduledNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, localNotificationListener.clearScheduledCount)
    }

    @Test
    fun testDownloadMissedNotifications() = runTest(testDispatcher) {
        val pushNotif = PushNotification(
            msgId = "missed_1",
            title = "Missed",
            body = "Body",
            type = PushNotification.Type.TEXT
        )
        networkService.missedNotifications = listOf(pushNotif)

        notificationManager.downloadMissedNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        val stored = repository.notification.getNotification("missed_1")
        assertNotNull(stored)
        assertEquals("Missed", stored.title)
    }

    @Test
    fun testCheckIfCompletedOrRead() = runTest(testDispatcher) {
        val observationId = "obs1"
        val deepLink = "app://more/test?observationId=$observationId"
        val notification =
            NotificationEntity(notificationId = "n1", deepLink = deepLink, read = true)

        repository.mockNotification.storeNotification(notification)
        repository.mockObservation.storeObservation(
            ObservationEntity(
                observationId = observationId
            )
        )
        repository.mockSchedule.storeSchedule(
            ScheduleEntity(
                observationId = observationId,
                state = io.redlink.more.models.ScheduleState.ACTIVE.name
            )
        )

        val status = notificationManager.checkIfCompletedOrRead(deepLink).first()
        assertEquals(NotificationStatusType.READ, status)
    }

    @Test
    fun testHandleNotificationDataAsync() = runTest(testDispatcher) {
        val data = mapOf(
            "key" to "STUDY_STATE_CHANGED",
            "MSG_ID" to "msg123",
            "oldState" to "active",
            "newState" to "paused"
        )
        notificationManager.handleNotificationDataAsync(data)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(actionObserver.updateStudyCalled)
        assertEquals(StudyState.ACTIVE, actionObserver.lastOldStudyState)
        assertEquals(StudyState.PAUSED, actionObserver.lastNewStudyState)
    }

}
