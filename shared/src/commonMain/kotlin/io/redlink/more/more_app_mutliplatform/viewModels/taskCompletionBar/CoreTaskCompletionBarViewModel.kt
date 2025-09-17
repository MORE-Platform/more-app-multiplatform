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
package io.redlink.more.more_app_mutliplatform.viewModels.taskCompletionBar

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.models.TaskCompletion
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine

class CoreTaskCompletionBarViewModel(private val repository: MainRepository) : CoreViewModel() {
    private val _taskCompletion: MutableStateFlow<TaskCompletion> =
        MutableStateFlow(TaskCompletion())

    @NativeCoroutines
    val taskCompletion: StateFlow<TaskCompletion> = _taskCompletion

    init {
        launchScope {
            repository.schedule.count()
                .combine(
                    repository.schedule.allSchedulesWithStatus(true).cancellable()
                ) { scheduleCount, doneSchedules ->
                    TaskCompletion(
                        doneSchedules.size,
                        scheduleCount
                    )
                }.cancellable().collect {
                    _taskCompletion.value = it
                }
        }
    }
}