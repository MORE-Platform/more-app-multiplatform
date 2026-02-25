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
package io.redlink.umm.blendedcare.app.android.activities.taskCompletion

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.participant.models.TaskCompletion
import io.redlink.umm.participant.viewModels.taskCompletionBar.CoreTaskCompletionBarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskCompletionBarViewModel : ViewModel() {
    private val coreViewModel =
        CoreTaskCompletionBarViewModel(BlendedCareApplication.Companion.shared!!.repositories)
    val taskCompletion: MutableState<TaskCompletion> = mutableStateOf(TaskCompletion())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.taskCompletion.collect {
                withContext(Dispatchers.Main) {
                    taskCompletion.value = it
                }
            }
        }
    }
}