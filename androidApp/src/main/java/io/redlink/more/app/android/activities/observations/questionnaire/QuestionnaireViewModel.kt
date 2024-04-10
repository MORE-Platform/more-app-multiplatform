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
package io.redlink.more.app.android.activities.observations.questionnaire

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion.SimpleQuestionCoreViewModel
import kotlinx.coroutines.*

class QuestionnaireViewModel : ViewModel() {
    private val coreViewModel: SimpleQuestionCoreViewModel = SimpleQuestionCoreViewModel(MoreApplication.shared!!.observationFactory)

    val hasData = mutableStateOf(false)
    val observationTitle = mutableStateOf("")
    val question = mutableStateOf("")
    var answers = mutableStateListOf("")
    val answerSet = mutableStateOf("")
    val observationParticipantInfo = mutableStateOf("")

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        scope.launch {
            coreViewModel.simpleQuestionModel.collect { model ->
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
        coreViewModel.finishQuestion(answerSet.value, setObservationToDone)
    }

    fun setAnswer(answer: String) {
        answerSet.value = answer
    }
}