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

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.models.QuestionType
import io.redlink.more.viewModels.simpleQuestion.QuestionCoreViewModel
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
        MoreApplication.shared!!.repositories,
        MoreApplication.shared!!.observationFactory,
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