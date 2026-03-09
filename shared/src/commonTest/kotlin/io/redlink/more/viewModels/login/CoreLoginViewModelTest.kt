package io.redlink.more.viewModels.login

import io.redlink.more.Shared
import io.redlink.more.mocks.InMemoryStorageRepository
import io.redlink.more.mocks.MockBluetoothConnector
import io.redlink.more.mocks.MockDataRecorder
import io.redlink.more.mocks.MockLocalNotificationListener
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockObservationFactory
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.models.LoginModel
import io.redlink.more.registration.RegistrationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoreLoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registrationService: MockRegistrationService
    private lateinit var viewModel: CoreLoginViewModel

    class MockRegistrationService(shared: Shared) : RegistrationService(shared) {
        var sendRegistrationTokenCalled = false
        var clearErrorCalled = false

        override fun sendRegistrationToken(loginModel: LoginModel) {
            sendRegistrationTokenCalled = true
        }

        override fun clearError() {
            clearErrorCalled = true
        }
    }

    private fun createMockShared(): Shared {
        return object : Shared(
            localNotificationListener = MockLocalNotificationListener(),
            repositories = MockMainRepository(),
            sharedStorageRepository = InMemoryStorageRepository(),
            observationDataManager = mockObservationDataManager(),
            mainBluetoothConnector = MockBluetoothConnector(),
            observationFactory = MockObservationFactory(MockMainRepository()),
            dataRecorder = MockDataRecorder()
        ) {}
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registrationService = MockRegistrationService(createMockShared())
        viewModel = CoreLoginViewModel(registrationService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSendRegistrationTokenValid() {
        val loginModel = LoginModel("token", "https://example.com")
        viewModel.sendRegistrationToken(loginModel)
        assertTrue(registrationService.sendRegistrationTokenCalled)
    }

    @Test
    fun testSendRegistrationTokenInvalid() {
        val loginModel = LoginModel("", "invalid-url")
        viewModel.sendRegistrationToken(loginModel)
        assertTrue(!registrationService.sendRegistrationTokenCalled)
    }

    @Test
    fun testClearError() {
        viewModel.clearError()
        assertTrue(registrationService.clearErrorCalled)
    }
}
