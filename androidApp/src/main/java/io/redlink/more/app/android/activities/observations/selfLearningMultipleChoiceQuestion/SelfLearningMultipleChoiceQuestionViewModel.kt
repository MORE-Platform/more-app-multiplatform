package io.redlink.more.app.android.activities.observations.selfLearningMultipleChoiceQuestion

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.viewModels.selfLearningMultipleChoiceQuestion.SelfLearningMultipleChoiceQuestionCoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelfLearningMultipleChoiceQuestionViewModel  : ViewModel() {
    private val coreViewModel: SelfLearningMultipleChoiceQuestionCoreViewModel = SelfLearningMultipleChoiceQuestionCoreViewModel(
        MoreApplication.shared!!.observationFactory,MoreApplication.shared!!.sharedStorageRepository)

    val hasData = mutableStateOf(false)
    val observationTitle = mutableStateOf("")
    val question = mutableStateOf("")
    var answers = mutableStateListOf("")
    val answerSet = mutableStateListOf<String>()
    val observationParticipantInfo = mutableStateOf("")
    val userTextAnswer = mutableStateOf("")

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        scope.launch {
            coreViewModel.selfLearningMultipleChoiceQuestionModel.collect { model ->
                withContext(Dispatchers.Main) {
                    model?.let {
                        hasData.value = true
                        observationTitle.value = it.observationTitle
                        question.value = it.question
                        answers.clear()
                        answers.addAll(it.answers)
                        observationParticipantInfo.value = it.participantInfo
                    } ?: kotlin.run {
                        hasData.value = false
                    }
                }
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
        hasData.value = false
    }

    fun setScheduleId(scheduleId: String, notificationId: String?) {
        coreViewModel.setScheduleId(scheduleId, notificationId)
    }

    fun setObservationId(observationId: String, notificationId: String?) {
        coreViewModel.setScheduleViaObservationId(observationId, notificationId)
    }

    fun finish(setObservationToDone: Boolean = true) {
        coreViewModel.finishQuestion(answerSet, userTextAnswer.value, setObservationToDone)
    }

    fun setAnswer(selectedValues: List<String>) {
        answerSet.clear()
        answerSet.addAll(selectedValues)
    }
}