package io.redlink.more.app.android.activities.taskCompletion

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.models.TaskCompletion
import io.redlink.more.more_app_mutliplatform.viewModels.taskCompletionBar.CoreTaskCompletionBarViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TaskCompletionBarViewModel {
    private val coreViewModel = CoreTaskCompletionBarViewModel()
    val taskCompletion: MutableState<TaskCompletion> = mutableStateOf(TaskCompletion())
    val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        scope.launch {
            coreViewModel.taskCompletion.collect {
                taskCompletion.value = it
            }
        }
    }
}