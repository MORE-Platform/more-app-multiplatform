package io.redlink.more.navigation

import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.models.ScheduleState
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.navigation.model.NavigationRouteParameter
import io.redlink.more.utils.MockMainRepository
import io.redlink.more.utils.MockObservationFactory
import io.redlink.more.utils.MockScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeeplinkManagerTest {

    private lateinit var deeplinkManager: DeeplinkManager

    private lateinit var mainRepository: MockMainRepository

    private lateinit var scheduleRepository: MockScheduleRepository

    private lateinit var observationFactory: MockObservationFactory

    private val testDispatcher = StandardTestDispatcher()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mainRepository = MockMainRepository()
        scheduleRepository = mainRepository.mockSchedule
        observationFactory = MockObservationFactory()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddAvailableDeepLinksAndRouteValidation() = runTest {
        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)

        val links = setOf("app://more/dashboard", "notifications")
        deeplinkManager.addAvailableDeepLinks(links)

        assertTrue(deeplinkManager.validateRoute("app://more/dashboard"))
        assertTrue(deeplinkManager.validateRoute("notifications"))
        assertTrue(deeplinkManager.validateRoute("/dashboard"))
    }

    @Test
    fun testSetProtocolAndSetHost() = runTest {
        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)
        deeplinkManager.setProtocol("https")
        deeplinkManager.setHost("redlink.io")

        val deepLink = "app://oldhost/dashboard"
        val result = deeplinkManager.modifyDeepLink(deepLink).first()

        assertNotNull(result)
        assertEquals("https://redlink.io/dashboard", result.route)
    }

    @Test
    fun testGetNotificationViewDeepLink() = runTest {
        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)
        val notificationId = "123"
        val result = deeplinkManager.getNotificationViewDeepLink(notificationId).first()

        assertNotNull(result)
        // Now using robust construction with defaults "app" and "more"
        val expected =
            "app://more/${NavigationRoute.NOTIFICATIONS.route}?${NavigationRouteParameter.NOTIFICATION_ID.key}=$notificationId"
        assertEquals(expected, result.route)
        assertEquals(notificationId, result.params[NavigationRouteParameter.NOTIFICATION_ID.key])
    }

    @Test
    fun testModifyDeepLinkWithScheduleId() = runTest {
        val scheduleId = "sched1"
        val observationId = "obs1"
        val now = Clock.System.now().epochSeconds
        val schedule = ScheduleEntity(
            scheduleId = scheduleId,
            observationId = observationId,
            observationType = "testType",
            start = now - 100,
            end = now + 100,
            state = ScheduleState.ACTIVE.name
        )

        scheduleRepository.scheduleWithIdResult = flowOf(schedule)

        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)

        val deepLink = "app://host/task-details?scheduleId=$scheduleId"
        val result = deeplinkManager.modifyDeepLink(deepLink).first()

        assertNotNull(result)
        assertEquals(scheduleId, result.params[NavigationRouteParameter.SCHEDULE_ID.key])
        assertEquals(observationId, result.params[NavigationRouteParameter.OBSERVATION_ID.key])
    }

    @Test
    fun testModifyDeepLinkWithObservationId() = runTest {
        val observationId = "obs1"
        val scheduleId = "sched1"
        val now = Clock.System.now().epochSeconds
        val schedule = ScheduleEntity(
            scheduleId = scheduleId,
            observationId = observationId,
            observationType = "testType",
            start = now - 100,
            end = now + 100,
            state = ScheduleState.ACTIVE.name
        )

        scheduleRepository.firstScheduleAvailableForObservationIdResult = flowOf(schedule)

        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)

        val deepLink = "app://host/observation-details?observationId=$observationId"
        val result = deeplinkManager.modifyDeepLink(deepLink).first()

        assertNotNull(result)
        assertEquals(observationId, result.params[NavigationRouteParameter.OBSERVATION_ID.key])
        assertEquals(scheduleId, result.params[NavigationRouteParameter.SCHEDULE_ID.key])
    }

    @Test
    fun testRouteMatchesWithSuffixes() = runTest {
        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)

        val registeredRoute = "question-observation"
        deeplinkManager.addAvailableDeepLinks(setOf("app://host/$registeredRoute"))

        val incomingDeepLink = "app://host/${registeredRoute}_response?observationId=123"
        val result = deeplinkManager.modifyDeepLink(incomingDeepLink).first()

        assertNotNull(result)
        assertTrue(result.route.contains(registeredRoute))
    }

    @Test
    fun testModifyDeepLinkNullInput() = runTest {
        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)
        val result = deeplinkManager.modifyDeepLink(null).first()
        assertNull(result)
    }

    @Test
    fun testCreateDeeplinkForSchedule() {
        observationFactory.matchingObservationTypes = setOf("question-observation")

        deeplinkManager = DeeplinkManager(mainRepository, observationFactory)
        val schedule = ScheduleEntity(
            scheduleId = "s1",
            observationId = "o1",
            observationType = "question-observation"
        )

        val result = deeplinkManager.createDeeplinkForSchedule(schedule)
        assertTrue(result.contains("scheduleId=s1"))
        assertTrue(result.contains("observationId=o1"))
        assertTrue(result.contains("question-observation"))
    }
}
