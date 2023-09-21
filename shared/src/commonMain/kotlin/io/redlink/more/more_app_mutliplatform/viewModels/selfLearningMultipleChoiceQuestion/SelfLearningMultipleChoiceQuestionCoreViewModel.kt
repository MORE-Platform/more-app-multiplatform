package io.redlink.more.more_app_mutliplatform.viewModels.selfLearningMultipleChoiceQuestion

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.models.SelfLearningMultipleChoiceQuestionModel
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull

class SelfLearningMultipleChoiceQuestionCoreViewModel(observationFactory: ObservationFactory,
                                                      private val sharedStorageRepository: SharedStorageRepository
) : CoreViewModel()  {
    private var scheduleId: String? = null
    private var answers : MutableList<String> = mutableListOf()
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    val selfLearningMultipleChoiceQuestionModel = MutableStateFlow<SelfLearningMultipleChoiceQuestionModel?>(null)
    private var observation: Observation? = null

    init {
        observationFactory.observation("self-learning-multiple-choice-question-observation")?.let {
            observation = it
        }
    }

    fun setScheduleId(scheduleId: String) {
        this.scheduleId = scheduleId
        answers.clear()
        answers.addAll(sharedStorageRepository.load(MULTIPLE_CHOICE_KEY,emptyList()))
        launchScope {
            scheduleRepository.scheduleWithId(scheduleId).cancellable().firstOrNull()?.let{ scheduleSchema ->
                observationRepository.observationById(scheduleSchema.observationId).cancellable().firstOrNull()?.let { observationSchema ->
                    selfLearningMultipleChoiceQuestionModel.emit(
                        SelfLearningMultipleChoiceQuestionModel.createModelFrom(
                            observationSchema,
                            scheduleId,
                            answers
                        )
                    )
                }
            }
        }
    }

    fun finishQuestion(data: List<String>, userTextAnswer: String, setObservationToDone: Boolean){
        if(userTextAnswer.isNotBlank())
        {
            answers.add(userTextAnswer)
            sharedStorageRepository.store(MULTIPLE_CHOICE_KEY,answers)
        }
        selfLearningMultipleChoiceQuestionModel.value?.let {
            observation?.let { observation ->
                observation.start(it.observationId, it.scheduleId)
                observation.storeData(mapOf("answer" to data)) {
                    scheduleId?.let {
                        observation.stopAndSetDone(it)
                    }
                }
            }
        }
    }

    fun onLoadSelfLearningMultipleChoiceQuestionObservation(provideNewState: ((SelfLearningMultipleChoiceQuestionModel?) -> Unit)): Closeable {
        return selfLearningMultipleChoiceQuestionModel.asClosure(provideNewState)
    }

    override fun viewDidAppear() {

    }

    companion object {
        private const val MULTIPLE_CHOICE_KEY = "sharedStorageSelfLearningMultipleChoiceKey"
    }
}