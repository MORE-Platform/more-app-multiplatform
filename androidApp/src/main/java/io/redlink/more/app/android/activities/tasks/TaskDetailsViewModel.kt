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
package io.redlink.more.app.android.activities.tasks

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.HR.PolarHeartRateObservation
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.viewModels.tasks.CoreTaskDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailsViewModel(
    dataRecorder: DataRecorder
) : ViewModel() {
    private val coreViewModel: CoreTaskDetailsViewModel = CoreTaskDetailsViewModel(dataRecorder)
    val isEnabled = mutableStateOf(false)
    val polarHrReady = mutableStateOf(false)
    val dataPointCount = mutableStateOf(0L)
    val taskDetailsModel = mutableStateOf(
        TaskDetailsModel(
            "", "", "", "", 0, 0, "", false, ScheduleState.DEACTIVATED
        )
    )

    val taskObservationErrors = mutableStateListOf<String>()
    val taskObservationErrorActions = mutableStateListOf<String>()
    private var observationErrors: Map<String, Set<String>> = emptyMap()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            PolarHeartRateObservation.hrReady.collect {
                withContext(Dispatchers.Main) {
                    polarHrReady.value = it
                }
            }
        }
        viewModelScope.launch {
            coreViewModel.taskDetailsModel.collect { details ->
                details?.let {
                    withContext(Dispatchers.Main) {
                        taskDetailsModel.value = it
                        isEnabled.value = it.state.active()
                        withContext(Dispatchers.Main) {
                            taskObservationErrors.clear()
                            taskObservationErrorActions.clear()
                            observationErrors[taskDetailsModel.value.observationType]?.let {
                                val (actions, messages) = it.partition { it == Observation.ERROR_DEVICE_NOT_CONNECTED }
                                taskObservationErrors.addAll(messages)
                                taskObservationErrorActions.addAll(actions)
                            }
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            coreViewModel.dataCount.collect {
                withContext(Dispatchers.Main) {
                    dataPointCount.value = it
                }
            }
        }

        viewModelScope.launch {
            MoreApplication.shared!!.observationFactory.observationErrors.collect {
                observationErrors = it
                if (taskDetailsModel.value.observationType != "") {
                    withContext(Dispatchers.Main) {
                        taskObservationErrors.clear()
                        taskObservationErrorActions.clear()
                        observationErrors[taskDetailsModel.value.observationType]?.let {
                            val (actions, messages) = it.partition { it == Observation.ERROR_DEVICE_NOT_CONNECTED }
                            taskObservationErrors.addAll(messages)
                            taskObservationErrorActions.addAll(actions)
                        }
                    }
                }
            }
        }
    }

    fun setSchedule(scheduleId: String) {
        coreViewModel.setSchedule(scheduleId)
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun startObservation() {
        coreViewModel.startObservation()
    }

    fun pauseObservation() {
        coreViewModel.pauseObservation()
    }

    fun stopObservation() {
        coreViewModel.stopObservation()
    }
}