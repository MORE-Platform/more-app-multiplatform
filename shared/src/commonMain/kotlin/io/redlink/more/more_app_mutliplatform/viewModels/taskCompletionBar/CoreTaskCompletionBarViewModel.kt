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

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.TaskCompletion
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CoreTaskCompletionBarViewModel: CoreViewModel() {
    val taskCompletion: MutableStateFlow<TaskCompletion> = MutableStateFlow(TaskCompletion())
    private val repository = ScheduleRepository()

    init {
        launchScope {
            repository.count()
                .combine(repository.allSchedulesWithStatus(true).cancellable()) { scheduleCount, doneSchedules ->
                    TaskCompletion(
                        doneSchedules.size,
                        scheduleCount.toInt()
                    )
                }.cancellable().collect {
                    taskCompletion.emit(it)
                }
        }
    }

    override fun viewDidAppear() {

    }

    fun onLoadTaskCompletion(provideNewState: ((taskCompletion: TaskCompletion) -> Unit)): Closeable {
        return taskCompletion.asClosure(provideNewState)
    }
}