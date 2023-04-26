package io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.SimpleQuestionModel
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation.SimpleQuestionObservation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SimpleQuestionCoreViewModel(
    private val scheduleId: String,
    observationFactory: ObservationFactory
) {
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    val simpleQuestionModel = MutableStateFlow<SimpleQuestionModel?>(null)
    private var observation: Observation? = null

    private val scope = CoroutineScope(Dispatchers.Default + Job())


    init {
        observationFactory.observation("question-observation")?.let {
            observation = it
        }
        scope.launch {
            scheduleRepository.scheduleWithId(scheduleId).firstOrNull()?.let{ scheduleSchema ->
                observationRepository.observationById(scheduleSchema.observationId).firstOrNull()?.let { observationSchema ->
                    simpleQuestionModel.emit(SimpleQuestionModel.createModelFrom(observationSchema))
                }
            }
        }
    }

    fun finishQuestion(data: String, setObservationToDone: Boolean){
        simpleQuestionModel.value?.observationId?.let { observationId ->
            observation?.let { observation ->
                observation.start(observationId, scheduleId)
                Napier.i("---------finishQuestion----------")
                Napier.i("observation started")
                observation.storeData(mapOf("answer" to data))
                Napier.i("---------finishQuestion----------")
                Napier.i("observation stored data")
                scheduleRepository.setCompletionStateFor(scheduleId, true)
                Napier.i("stored data")
                observation.stop(observationId)
                Napier.i("observation stop observation")
            }
        }
    }

    fun onLoadSimpleQuestionObservation(provideNewState: ((SimpleQuestionModel?) -> Unit)): Closeable {
        return simpleQuestionModel.asClosure(provideNewState)
    }
}
