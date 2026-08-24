package io.redlink.more.registration

import io.redlink.more.Shared
import io.redlink.more.mocks.InMemoryStorageRepository
import io.redlink.more.mocks.MockBluetoothConnector
import io.redlink.more.mocks.MockDataRecorder
import io.redlink.more.mocks.MockLocalNotificationListener
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockObservationFactory
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.models.LoginModel
import io.redlink.more.scopes.AppDispatchers
import io.redlink.more.services.network.MockNetworkWatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationServiceTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registrationService: RegistrationService

    private fun createMockShared(): Shared {
        val mockRepo = MockMainRepository()
        return object : Shared(
            localNotificationListener = MockLocalNotificationListener(),
            repositories = mockRepo,
            sharedStorageRepository = InMemoryStorageRepository(),
            observationDataManager = mockObservationDataManager(mockRepo),
            mainBluetoothConnector = MockBluetoothConnector(),
            observationFactory = MockObservationFactory(mockRepo),
            dataRecorder = MockDataRecorder(),
            networkWatcher = MockNetworkWatcher(),
            connectionStatusFlow = MutableStateFlow(true),
            isDebug = true
        ) {}
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        AppDispatchers.set(testDispatcher, testDispatcher, testDispatcher)
        registrationService = RegistrationService(createMockShared())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        AppDispatchers.reset()
    }

    @Test
    fun `given new consent flow when beginConsentSubmission then isLoading is true`() = runTest {
        assertFalse(registrationService.isLoading.value)

        registrationService.beginConsentSubmission()

        assertTrue(registrationService.isLoading.value)
    }

    @Test
    fun `given in progress consent submission when cancelConsentSubmission then isLoading is false`() =
        runTest {
            registrationService.beginConsentSubmission()
            assertTrue(registrationService.isLoading.value)

            registrationService.cancelConsentSubmission()

            assertFalse(registrationService.isLoading.value)
        }

    @Test
    fun `given missing login and study when acceptConsent then isLoading is reset to false immediately`() =
        runTest {
            registrationService.beginConsentSubmission()
            assertTrue(registrationService.isLoading.value)

            registrationService.acceptConsent("test-device-id")

            assertFalse(registrationService.isLoading.value)
        }

    @Test
    fun `given valid login but missing study when acceptConsent then isLoading is reset to false immediately`() =
        runTest {
            val validLoginFlow =
                registrationService.validLoginModel as MutableStateFlow<LoginModel?>
            validLoginFlow.value = LoginModel("token", "https://example.com")

            registrationService.beginConsentSubmission()
            assertTrue(registrationService.isLoading.value)

            registrationService.acceptConsent("test-device-id")

            assertFalse(registrationService.isLoading.value)
        }

    @Test
    fun `given valid login and study when acceptConsent then isLoading transitions to false once network completes`() =
        runTest {
            registrationService.sendRegistrationToken(
                LoginModel(
                    "DEMO",
                    "https://demo.more-platform.org"
                )
            )
            runCurrent()

            assertNotNull(registrationService.validLoginModel.value)
            assertNotNull(registrationService.study.value)

            registrationService.beginConsentSubmission()
            assertTrue(registrationService.isLoading.value)

            registrationService.acceptConsent("test-device-id")
            runCurrent()

            assertFalse(registrationService.isLoading.value)
        }
}
