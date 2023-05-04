package io.redlink.more.more_app_mutliplatform.viewModels.settings

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CoreSettingsViewModel(
    private val shared: Shared
): CoreViewModel() {
    val dataDeleted = MutableStateFlow(false)
    private val studyRepository: StudyRepository = StudyRepository()
    private val observationRepository = ObservationRepository()

    val study = MutableStateFlow<StudySchema?>(null)
    val observations = MutableStateFlow(emptyList<ObservationSchema>())

    val permissionModel = MutableStateFlow<PermissionModel?>(null)

    override fun viewDidAppear() {
        launchScope {
            studyRepository.getStudy()
                .combine(observationRepository.observations()) { study, observations ->
                    Pair(study, observations)
                }.collect {
                    study.value = it.first
                    it.first?.let { study ->
                        permissionModel.value = PermissionModel.createFromSchema(study, it.second)
                    }
                }
        }
        launchScope {
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
        viewModelScope.launch {
            shared.networkService.deleteParticipation()
            shared.credentialRepository.remove()
            shared.endpointRepository.removeEndpoint()
            DatabaseManager.deleteAll()
            dataDeleted.value = true
        }
    }

}