package io.redlink.more.more_app_mutliplatform.viewModels.taskCompletionBar

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.TaskCompletion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CoreTaskCompletionBarViewModel {
    val taskCompletion: MutableStateFlow<TaskCompletion> = MutableStateFlow(TaskCompletion())
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val repository = ScheduleRepository()
    init {
        scope.launch {
            repository.allSchedules()
                .combine(repository.allSchedulesWithStatus(true)) { scheduleCount, doneSchedules ->
                    TaskCompletion(
                        doneSchedules.size,
                        scheduleCount.toInt()
                    )
                }.collect {
                    taskCompletion.emit(it)
                }
        }
    }

    fun onLoadTaskCompletion(provideNewState: ((taskCompletion: TaskCompletion) -> Unit)): Closeable {
        return taskCompletion.asClosure(provideNewState)
    }
}