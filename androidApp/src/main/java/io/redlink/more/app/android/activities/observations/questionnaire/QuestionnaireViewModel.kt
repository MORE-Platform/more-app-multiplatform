package io.redlink.more.app.android.activities.observations.questionnaire

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion.SimpleQuestionCoreViewModel
import kotlinx.coroutines.*

class QuestionnaireViewModel : ViewModel() {
    private val coreViewModel: SimpleQuestionCoreViewModel = SimpleQuestionCoreViewModel(MoreApplication.observationFactory!!)

    val observationTitle = mutableStateOf("")
    val question = mutableStateOf("")
    var answers = mutableStateListOf("")
    val answerSet = mutableStateOf("")
    val observationParticipantInfo = mutableStateOf("")

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        scope.launch {
            coreViewModel.simpleQuestionModel.collect { model ->
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
        coreViewModel.finishQuestion(answerSet.value, setObservationToDone)
    }

    fun setAnswer(answer: String) {
        answerSet.value = answer
    }
}