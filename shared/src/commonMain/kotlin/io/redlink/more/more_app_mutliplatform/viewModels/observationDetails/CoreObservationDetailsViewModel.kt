package io.redlink.more.more_app_mutliplatform.viewModels.observationDetails

import io.ktor.utils.io.core.*
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.models.ObservationDetailsModel
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.settings.CoreSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

class CoreObservationDetailsViewModel(
    private val observationId: String
) : CoreViewModel() {
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    val observationDetailsModel = MutableStateFlow<ObservationDetailsModel?>(null)

    override fun viewDidAppear() {
        launchScope {
            observationRepository.observationById(observationId)
                .combine(scheduleRepository.getFirstAndLastDate(observationId)) { observation, pair ->
                    Triple(
                        observation,
                        pair.first,
                        pair.second
                    )
                }.collect { triple ->
                    triple.first?.let { observation ->
                        observationDetailsModel.value = ObservationDetailsModel.createModelFrom(
                            observation,
                            triple.second,
                            triple.third
                        )
                    }
                }
        }
    }

    fun onLoadObservationDetails(provideNewState: ((ObservationDetailsModel?) -> Unit)): Closeable {
        return observationDetailsModel.asClosure(provideNewState)
    }
}