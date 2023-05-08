package io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.SimpleQuestionModel
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull

class SimpleQuestionCoreViewModel(
    private val scheduleId: String,
    observationFactory: ObservationFactory
): CoreViewModel() {
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    val simpleQuestionModel = MutableStateFlow<SimpleQuestionModel?>(null)
    private var observation: Observation? = null

    init {
        observationFactory.observation("question-observation")?.let {
            observation = it
        }
        launchScope {
            scheduleRepository.scheduleWithId(scheduleId).firstOrNull()?.let{ scheduleSchema ->
                observationRepository.observationById(scheduleSchema.observationId).firstOrNull()?.let { observationSchema ->
                    simpleQuestionModel.emit(SimpleQuestionModel.createModelFrom(observationSchema, scheduleId))
                }
            }
        }
    }



    fun finishQuestion(data: String, setObservationToDone: Boolean){
        simpleQuestionModel.value?.let {
            observation?.let { observation ->
                observation.start(it.observationId, it.scheduleId)
                observation.storeData(mapOf("answer" to data)) {
                    observation.stopAndSetDone()
                }
            }
        }
    }

    fun onLoadSimpleQuestionObservation(provideNewState: ((SimpleQuestionModel?) -> Unit)): Closeable {
        return simpleQuestionModel.asClosure(provideNewState)
    }

    override fun viewDidAppear() {

    }
}
