package io.redlink.more.viewModels.permission

import io.redlink.more.Shared
import io.redlink.more.mocks.InMemoryStorageRepository
import io.redlink.more.mocks.MockBluetoothConnector
import io.redlink.more.mocks.MockDataRecorder
import io.redlink.more.mocks.MockLocalNotificationListener
import io.redlink.more.mocks.MockMainRepository
import io.redlink.more.mocks.MockObservationFactory
import io.redlink.more.mocks.mockObservationDataManager
import io.redlink.more.registration.RegistrationService
import io.redlink.more.services.network.openapi.model.Study
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CoreConsentViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registrationService: RegistrationService
    private lateinit var viewModel: CoreConsentViewModel
    private val studyConsentTitle = "Study Consent"

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
            connectionStatusFlow = flowOf(true)
        ) {}
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registrationService = RegistrationService(createMockShared())
        viewModel = CoreConsentViewModel(registrationService, studyConsentTitle)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPermissionsUpdateWhenStudyChanges() = runTest {
        val study = Study(
            studyTitle = "Test Study",
            participantInfo = "Participant Info",
            consentInfo = "Consent Info",
            start = LocalDate(2023, 1, 1),
            end = LocalDate(2023, 12, 31),
            observations = emptyList(),
            version = 1L
        )

        val registrationServiceStudy = registrationService.study as MutableStateFlow

        assertNull(viewModel.permissions.value)

        registrationServiceStudy.value = study
        runCurrent()

        val permissions = viewModel.permissions.value
        assertNotNull(permissions)
        assertEquals("Test Study", permissions.studyTitle)
        assertEquals("Consent Info", permissions.studyConsentInfo)
        assertEquals(1, permissions.consentInfo.size)
        assertEquals(studyConsentTitle, permissions.consentInfo[0].title)
    }
}
