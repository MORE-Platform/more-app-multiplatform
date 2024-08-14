package io.redlink.more.more_app_mutliplatform.viewModels.selfLearningMultipleChoiceQuestion

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.models.SelfLearningMultipleChoiceQuestionModel
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SelfLearningMultipleChoiceQuestionObservationType
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SimpleQuestionType
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
    private var observation: Observation? = observationFactory.observation(
        SelfLearningMultipleChoiceQuestionObservationType().observationType)

    private var notificationId: String? = null

    fun setScheduleId(scheduleId: String, notificationId: String? = null) {
        this.scheduleId = scheduleId
        this.notificationId = notificationId
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

    fun setScheduleViaObservationId(observationId: String, notificationId: String? = null) {
        launchScope {
            scheduleRepository.firstScheduleIdAvailableForObservationId(observationId).cancellable().firstOrNull()?.let { setScheduleId(it, notificationId)}
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
                observation.start(it.observationId, it.scheduleId, notificationId)
                observation.storeData(mapOf("answer" to data)) {
                    scheduleId?.let {
                        observation.stopAndSetDone(it)
                    }
                }
                notificationId = null
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