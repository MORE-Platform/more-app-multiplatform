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

package io.redlink.more.observations.appUsage

import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockSharedStorageRepository
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.services.store.PermissionRepositoryImpl
import io.redlink.more.services.store.PermissionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppUsageObservationTest {

    @Test
    fun testInstantEvent() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, true)

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))
        observation.start("1", "1")

        val event = LogEvent.URL_OPEN
        observation.onEvent(event, "https://redlink.at")

        assertEquals(1, mockRepo.mockObservationData.addedData.size)
        val storedData = mockRepo.mockObservationData.addedData.first()
        assertTrue(storedData.dataValue.contains("url_open"))
        // Check for presence of string without colons or dots that might be escaped
        assertTrue(storedData.dataValue.contains("redlink"))
    }

    @Test
    fun testRangeEvent() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, true)

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))
        observation.start("1", "1")

        observation.onEvent(LogEvent.VIEW_OPEN, "test_view")
        assertEquals(0, mockRepo.mockObservationData.addedData.size)

        observation.onEvent(LogEvent.VIEW_CLOSED, "test_view")

        assertEquals(1, mockRepo.mockObservationData.addedData.size)
        val storedData = mockRepo.mockObservationData.addedData.first()
        assertTrue(storedData.dataValue.contains("view_visibility"))
        assertTrue(storedData.dataValue.contains("test_view"))
    }

    @Test
    fun testTrackingDeclined() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, false)

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))
        observation.start("1", "1")

        observation.onEvent(LogEvent.URL_OPEN, "https://example.com")
        assertEquals(0, mockRepo.mockObservationData.addedData.size)
    }

    @Test
    fun testStoreWithoutApproval() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, false)

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))
        observation.start("1", "1")

        // BUTTON_PRESS has storeWithoutApproval = true now
        observation.onEvent(LogEvent.BUTTON_PRESS, "test_button")

        assertEquals(1, mockRepo.mockObservationData.addedData.size)
        val storedData = mockRepo.mockObservationData.addedData.first()
        assertTrue(storedData.dataValue.contains("button_press"))
    }

    @Test
    fun testSendAfterApproval() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, false)

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))
        observation.start("1", "1")

        // VIEW_OPEN has storeWithoutApproval = false
        observation.onEvent(LogEvent.VIEW_OPEN, "test_view")
        assertEquals(0, mockRepo.mockObservationData.addedData.size)

        // Accept tracking
        observation.onEvent(LogEvent.APP_TRACKING_ACCEPTED, null)

        // Close view - now it should be stored because tracking is approved
        observation.onEvent(LogEvent.VIEW_CLOSED, "test_view")
        // It should be 2 now because APP_TRACKING_ACCEPTED is also stored!
        // and it flushes the buffered VIEW_OPEN
        assertEquals(2, mockRepo.mockObservationData.addedData.size)
        assertTrue(mockRepo.mockObservationData.addedData.any { it.dataValue.contains("view_visibility") })
        assertTrue(mockRepo.mockObservationData.addedData.any { it.dataValue.contains("app_tracking_accepted") })
    }

    @Test
    fun testPersistenceAcrossRestarts() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, false)

        // First session: Tracking is declined, we log an event
        val observation1 = AppUsageObservation(mockRepo, permissionRepo)
        observation1.setDataManager(mockObservationDataManager(mockRepo))
        observation1.start("1", "1")

        observation1.onEvent(LogEvent.URL_OPEN, "https://example.com/buffered")
        assertEquals(0, mockRepo.mockObservationData.addedData.size)
        // Check if it's in shared storage
        assertTrue(mockSharedStorage.load("app_usage_data_buffer", "").contains("url_open"))

        // Restart session: Tracking is still declined, we log another event
        val observation2 = AppUsageObservation(mockRepo, permissionRepo)
        observation2.setDataManager(mockObservationDataManager(mockRepo))
        observation2.start("1", "1")

        observation2.onEvent(LogEvent.URL_OPEN, "https://example.com/buffered2")
        assertEquals(0, mockRepo.mockObservationData.addedData.size)
        assertTrue(mockSharedStorage.load("app_usage_data_buffer", "").contains("buffered"))
        assertTrue(mockSharedStorage.load("app_usage_data_buffer", "").contains("buffered2"))

        // Finally accept tracking
        observation2.onEvent(LogEvent.APP_TRACKING_ACCEPTED, null)

        // Both buffered events + APP_TRACKING_ACCEPTED should be stored now
        assertEquals(3, mockRepo.mockObservationData.addedData.size)
        val dataValues = mockRepo.mockObservationData.addedData.map { it.dataValue }
        assertTrue(dataValues.any { it.contains("buffered") })
        assertTrue(dataValues.any { it.contains("buffered2") })
        assertTrue(dataValues.any { it.contains("app_tracking_accepted") })

        // Shared storage should be cleared
        assertEquals("NOT_SET", mockSharedStorage.load("app_usage_data_buffer", "NOT_SET"))
    }

    @Test
    fun testBufferUntilStart() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, true)

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))

        // No start("1", "1") yet!

        observation.onEvent(LogEvent.URL_OPEN, "https://redlink.at")

        // Should be buffered even if tracking is approved
        assertEquals(0, mockRepo.mockObservationData.addedData.size)
        assertTrue(mockSharedStorage.load("app_usage_data_buffer", "").contains("url_open"))

        // Now start
        observation.start("1", "1")

        // Should be flushed
        assertEquals(1, mockRepo.mockObservationData.addedData.size)
        assertTrue(mockRepo.mockObservationData.addedData.first().dataValue.contains("url_open"))
    }

    @Test
    fun testStoreWithoutApprovalBufferedUntilStart() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, false) // Declined

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))

        // BUTTON_PRESS has storeWithoutApproval = true
        observation.onEvent(LogEvent.BUTTON_PRESS, "test_button")

        // Should be buffered because not started
        assertEquals(0, mockRepo.mockObservationData.addedData.size)
        assertTrue(mockSharedStorage.load("app_usage_data_buffer", "").contains("button_press"))

        // Now start
        observation.start("1", "1")

        // Should be flushed even if tracking is NOT approved
        assertEquals(1, mockRepo.mockObservationData.addedData.size)
        assertTrue(mockRepo.mockObservationData.addedData.first().dataValue.contains("button_press"))
    }

    @Test
    fun testOnStudyExitClearsBuffer() {
        val mockRepo = MockMainRepository()
        val mockSharedStorage = MockSharedStorageRepository()
        val permissionRepo = PermissionRepositoryImpl(mockSharedStorage)
        permissionRepo.updatePermission(PermissionType.APP_TRACKING, true)

        val observation = AppUsageObservation(mockRepo, permissionRepo)
        observation.setDataManager(mockObservationDataManager(mockRepo))

        // Buffer an event (by not starting observation)
        observation.onEvent(LogEvent.URL_OPEN, "https://redlink.at")
        assertTrue(mockSharedStorage.load("app_usage_data_buffer", "").contains("url_open"))

        // Start an observation range
        observation.onEvent(LogEvent.VIEW_OPEN, "test_view")

        // Call onStudyExit
        observation.onStudyExit()

        // Buffer should be cleared from shared storage
        assertEquals("NOT_SET", mockSharedStorage.load("app_usage_data_buffer", "NOT_SET"))

        // Now start the observation - nothing should be flushed because it was cleared
        observation.start("1", "1")
        assertEquals(0, mockRepo.mockObservationData.addedData.size)

        // Close the view - nothing should be stored because storage was cleared and not flushed
        observation.onEvent(LogEvent.VIEW_CLOSED, "test_view")
        assertEquals(0, mockRepo.mockObservationData.addedData.size)
    }
}
