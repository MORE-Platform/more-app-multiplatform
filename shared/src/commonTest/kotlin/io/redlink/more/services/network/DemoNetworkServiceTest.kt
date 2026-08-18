package io.redlink.more.services.network

import io.redlink.more.models.LoginModel
import io.redlink.more.observations.healthConnect.HealthConnectDataType
import io.redlink.more.services.network.demo.DemoNetworkService
import io.redlink.more.services.network.openapi.model.StudyConsent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DemoNetworkServiceTest {

    private val demoNetworkService = DemoNetworkService()

    @Test
    fun testValidateRegistrationToken_Demo() = runTest {
        val loginModel = LoginModel(token = "DEMO", endpoint = "https://demo.more-platform.org")
        val (study, error) = demoNetworkService.validateRegistrationToken(loginModel)

        assertNotNull(study)
        assertNull(error)
        assertEquals("Demo Study", study.studyTitle)
    }

    @Test
    fun testValidateRegistrationToken_Invalid() = runTest {
        val loginModel = LoginModel(token = "INVALID", endpoint = "https://demo.more-platform.org")
        val (study, error) = demoNetworkService.validateRegistrationToken(loginModel)

        assertNull(study)
        assertNotNull(error)
        assertEquals(404, error.code)
    }

    @Test
    fun testGetStudyConfig() = runTest {
        val (study, error) = demoNetworkService.getStudyConfig(null)

        assertNotNull(study)
        assertNull(error)
        assertEquals("Demo Study", study.studyTitle)
    }

    @Test
    fun testSendConsent() = runTest {
        val loginModel = LoginModel(token = "DEMO", endpoint = "https://demo.more-platform.org")
        val (config, error) = demoNetworkService.sendConsent(
            loginModel,
            StudyConsent(
                consent = true,
                observations = emptyList(),
                consentInfoMD5 = "",
                deviceId = ""
            )
        )

        assertNotNull(config)
        assertNull(error)
        assertEquals("DEMO_ID", config.credentials.apiId)
        assertEquals("DEMO_KEY", config.credentials.apiKey)
    }

    @Test
    fun testHealthConnectObservationsInStudy() = runTest {
        val (study, _) = demoNetworkService.getStudyConfig(null)
        assertNotNull(study)
        val observations = study.observations

        val heartRateObservation = observations.find { it.observationId == "9" }
        assertNotNull(heartRateObservation)
        assertEquals(HealthConnectDataType.HEART_RATE.subTypeValue, heartRateObservation.observationType)

        val stepsObservation = observations.find { it.observationId == "10" }
        assertNotNull(stepsObservation)
        assertEquals(HealthConnectDataType.STEPS.subTypeValue, stepsObservation.observationType)
    }

    @Test
    fun testDownloadMissedNotifications() = runTest {
        val notifications = demoNetworkService.downloadMissedNotifications()
        assertEquals(5, notifications.size)

        assertEquals("demo_1", notifications[0].msgId)
        assertEquals("Welcome to the Study!", notifications[0].title)
        assertNull(notifications[0].deepLink)

        assertEquals("demo_2", notifications[1].msgId)
        assertEquals("Daily Mood Check", notifications[1].title)
        assertEquals("more://task-details?observationId=1", notifications[1].deepLink)

        assertEquals("demo_3", notifications[2].msgId)
        assertEquals("New Observation available", notifications[2].title)
        assertEquals("more://task-details?observationId=2", notifications[2].deepLink)
    }
}
