package io.redlink.more.more_app_mutliplatform.observations.extensions.pushButtonObservation

import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class PushButtonCoreViewModel(
    observationFactory: ObservationFactory
): CoreViewModel() {
    private var scheduleId: String? = null
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    val pushButtonModel = MutableStateFlow<PushButtonModel?>(null)
    private var observation: Observation? = null

    init {
        observationFactory.observation("push-button-observation")?.let {
            observation = it
        }
    }

    fun setScheduleId(scheduleId: String) {
        this.scheduleId = scheduleId
        launchScope {
            scheduleRepository.scheduleWithId(scheduleId).cancellable().firstOrNull()?.let{ scheduleSchema ->
                observationRepository.observationById(scheduleSchema.observationId).cancellable().firstOrNull()?.let { observationSchema ->
                    pushButtonModel.emit(
                        PushButtonModel(
                        getButtonText(observationSchema),
                        observationSchema.participantInfo,
                        observationSchema.observationId,
                        observationSchema.observationTitle,
                        scheduleId
                    )
                    )
                }
            }
        }
    }

    fun finish(){
        observation?.let { observation ->
            scheduleId?.let {
                observation.stopAndSetDone(it)
            }
        }
    }

    fun click(data: Int){
        pushButtonModel.value?.let {
            observation?.let { observation ->
                observation.start(it.observationId, it.scheduleId)
                observation.storeData(mapOf("i" to data)) {
                    observation.finish()
                }
            }
        }
    }

    override fun viewDidAppear() {

    }

    private fun getButtonText(observationSchema: ObservationSchema): String {
        val config: Map<String, JsonElement> =
            observationSchema.configuration?.let { config ->
                Json.decodeFromString<JsonObject>(config).toMap()
            } ?: emptyMap()
        return config["buttonText"]?.toString()?.trim('\"') ?: ""
    }
}