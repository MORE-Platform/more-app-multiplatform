package io.redlink.more.more_app_mutliplatform.android.activities.tasks

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TaskDetailsViewModel(coreScheduleViewModel: CoreScheduleViewModel) {

    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(coreScheduleViewModel)
    private val dataPointCount: MutableState<Long> = mutableStateOf(0)

    fun getDataPointCount(scheduleId: String): Long {
        var count: Long = 0
        CoroutineScope(Dispatchers.Default + Job()).launch {
            coreViewModel.loadDataCount(scheduleId).collect {
                it?.let {
                    count = it.count
                }
            }
        }
        return count
    }
}