package io.redlink.more.app.android.activities.taskCompletion

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.models.TaskCompletion
import io.redlink.more.more_app_mutliplatform.viewModels.taskCompletionBar.CoreTaskCompletionBarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskCompletionBarViewModel: ViewModel() {
    private val coreViewModel = CoreTaskCompletionBarViewModel()
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