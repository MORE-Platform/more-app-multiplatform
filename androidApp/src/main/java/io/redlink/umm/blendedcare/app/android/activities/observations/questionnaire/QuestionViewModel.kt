package io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.participant.models.QuestionType
import io.redlink.umm.participant.viewModels.simpleQuestion.QuestionCoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestionViewModel(
    scheduleId: String?,
    notificationId: String?,
    observationId: String?
) :
    ViewModel() {
    val coreViewModel: QuestionCoreViewModel = QuestionCoreViewModel(
        BlendedCareApplication.shared!!.repositories,
        BlendedCareApplication.shared!!.observationFactory,
        scheduleId,
        notificationId,
        observationId
    )

    val hasData = mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            coreViewModel.questionModel.collect { model ->
                withContext(Dispatchers.Main) {
                    hasData.value = model?.isValidModel() ?: false
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

    fun finish(type: QuestionType, answer: Any) {
        coreViewModel.finishQuestion(answer)
    }
}