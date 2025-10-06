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
package io.redlink.more.app.android.activities.dashboard.schedule

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.services.bluetooth.polar.PolarStates
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleViewModel(
    val scheduleListType: ScheduleListType
) : ViewModel() {
    private val coreDashboardFilterViewModel =
        CoreDashboardFilterViewModel(MoreApplication.shared!!.repositories)

    val coreViewModel = CoreScheduleViewModel(
        MoreApplication.shared!!.repositories,
        MoreApplication.shared!!.dataRecorder,
        scheduleListType = scheduleListType,
        coreFilterModel = coreDashboardFilterViewModel
    )

    val polarHrReady: MutableState<Boolean> = mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            PolarStates.hrFeatureReady.collect {
                withContext(Dispatchers.Main) {
                    polarHrReady.value = it
                }
            }
        }
    }

    fun startObservation(scheduleId: String) {
        coreViewModel.start(scheduleId)
    }

    fun pauseObservation(scheduleId: String) {
        coreViewModel.pause(scheduleId)
    }
}