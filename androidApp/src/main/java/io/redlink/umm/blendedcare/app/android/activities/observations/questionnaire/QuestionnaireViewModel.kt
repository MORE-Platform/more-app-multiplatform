/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.participant.viewModels.simpleQuestion.SimpleQuestionCoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestionnaireViewModel(scheduleId: String?, notificationId: String?, observationId: String?) : ViewModel() {
    val coreViewModel: SimpleQuestionCoreViewModel = SimpleQuestionCoreViewModel(
        BlendedCareApplication.shared!!.repositories,
        BlendedCareApplication.shared!!.observationFactory,
        scheduleId,
        notificationId,
        observationId
    )

    val hasData = mutableStateOf(false)
    val answerSet = mutableStateOf("")

    init {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            coreViewModel.simpleQuestionModel.collect { model ->
                withContext(Dispatchers.Main) {
                    model?.let {
                        hasData.value = true
                    } ?: run {
                        hasData.value = false
                    }
                }
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
        answerSet.value = ""
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
        hasData.value = false
    }

    fun finish(setObservationToDone: Boolean = true) {
        coreViewModel.finishQuestion(answerSet.value, setObservationToDone)
    }

    fun setAnswer(answer: String) {
        answerSet.value = answer
    }
}