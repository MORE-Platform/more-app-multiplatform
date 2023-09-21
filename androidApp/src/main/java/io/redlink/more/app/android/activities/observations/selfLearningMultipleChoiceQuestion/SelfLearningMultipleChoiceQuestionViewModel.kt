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
                model?.let {
                    withContext(Dispatchers.Main) {
                        observationTitle.value = it.observationTitle
                        question.value = it.question
                        answers.clear()
                        answers.addAll(it.answers)
                        observationParticipantInfo.value = it.participantInfo
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
    }

    fun setScheduleId(scheduleId: String) {
        coreViewModel.setScheduleId(scheduleId)
    }

    fun finish(setObservationToDone: Boolean = true) {
        coreViewModel.finishQuestion(answerSet, userTextAnswer.value, setObservationToDone)
    }

    fun setAnswer(selectedValues: List<String>) {
        answerSet.clear()
        answerSet.addAll(selectedValues)
    }
}