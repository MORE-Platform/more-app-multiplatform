package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.jvmLocalDate
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduleViewModel: ViewModel() {
    private val coreViewModel = CoreScheduleViewModel()

    val schedules = mutableStateMapOf<LocalDate, List<ScheduleModel>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.scheduleModelList.collect{ map ->
                val javaConvertedMap = map.mapKeys { it.key.jvmLocalDate() }
                withContext(Dispatchers.Main) {
                    updateData(javaConvertedMap)
                }
            }
        }
    }

    private fun updateData(data: Map<LocalDate, List<ScheduleModel>>) {
        schedules.clear()
        schedules.putAll(data)
    }
}