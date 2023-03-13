package io.redlink.more.more_app_mutliplatform.viewModels.settings

import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreSettingsViewModel(
    private val credentialRepository: CredentialRepository,
    private val endpointRepository: EndpointRepository
) {
    val dataDeleted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val studyRepository: StudyRepository = StudyRepository()

    private val networkService: NetworkService = NetworkService(endpointRepository, credentialRepository)
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    fun loadStudy(): MutableStateFlow<StudySchema?> {
        val study: MutableStateFlow<StudySchema?> = MutableStateFlow(StudySchema())
        CoroutineScope(Dispatchers.Default + Job()).launch {
            studyRepository.getStudy().collect {
                study.value = it
            }
        }
        return study
    }

    fun onLoadStudy(provideNewState: ((StudySchema?) -> Unit)): Closeable {
        val job = Job()
        loadStudy().onEach {
            provideNewState(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))
        return object: Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }

    fun exitStudy() {
        scope.launch {
            networkService.deleteParticipation()
            credentialRepository.remove()
            endpointRepository.removeEndpoint()
            DatabaseManager.deleteAll()
            dataDeleted.value = true
        }
    }

    fun reloadStudyConfig() {
        scope.launch {

        }
    }
}