package io.redlink.more.more_app_mutliplatform.viewModels.settings

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CoreSettingsViewModel(
    private val credentialRepository: CredentialRepository,
    private val endpointRepository: EndpointRepository
) {
    val dataDeleted = MutableStateFlow(false)
    private val studyRepository: StudyRepository = StudyRepository()
    private val observationRepository = ObservationRepository()

    val study = MutableStateFlow<StudySchema?>(null)
    val observations = MutableStateFlow(emptyList<ObservationSchema>())

    val permissionModel = MutableStateFlow<PermissionModel?>(null)

    private val networkService: NetworkService =
        NetworkService(endpointRepository, credentialRepository)
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    init {
        scope.launch {
            studyRepository.getStudy()
                .combine(observationRepository.observations()) { study, observations ->
                    Pair(
                        study,
                        observations
                    )
                }.collect {
                it.first?.let { study ->
                    permissionModel.value = PermissionModel.createFromSchema(study, it.second)
                }
            }
        }
        scope.launch {
            studyRepository.getStudy().collect {
                study.value = it
            }
        }
        scope.launch {
            observationRepository.observations().collect {
                observations.value = it
            }
        }
    }

    fun onLoadStudy(provideNewState: ((StudySchema?) -> Unit)): Closeable {
        return study.asClosure(provideNewState)
    }

    fun onPermissionChange(provideNewState: (PermissionModel?) -> Unit): Closeable {
        return permissionModel.asClosure(provideNewState)
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