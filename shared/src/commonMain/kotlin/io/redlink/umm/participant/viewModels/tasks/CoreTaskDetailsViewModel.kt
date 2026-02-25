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
package io.redlink.umm.participant.viewModels.tasks

import io.ktor.utils.io.core.Closeable
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.extensions.asClosure
import io.redlink.umm.participant.models.TaskDetailsModel
import io.redlink.umm.participant.observations.DataRecorder
import io.redlink.umm.participant.observations.Observation
import io.redlink.umm.participant.observations.ObservationStates
import io.redlink.umm.participant.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull

class CoreTaskDetailsViewModel(
    private val repository: MainRepository,
    private val dataRecorder: DataRecorder,
    private var scheduleId: String
) : CoreViewModel() {

    private val _taskDetailsModel = MutableStateFlow<TaskDetailsModel?>(null)
    val taskDetailsModel: StateFlow<TaskDetailsModel?> = _taskDetailsModel
    private val _dataCount = MutableStateFlow<Long>(0)
    val dataCount: StateFlow<Long> = _dataCount
    private val _observationErrors = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val observationErrors: StateFlow<Map<String, Set<String>>> = _observationErrors
    private val _taskObservationErrors = MutableStateFlow<List<String>>(emptyList())
    val taskObservationErrors: StateFlow<List<String>> = _taskObservationErrors
    private val _taskObservationErrorActions = MutableStateFlow<List<String>>(emptyList())
    val taskObservationErrorActions: StateFlow<List<String>> = _taskObservationErrorActions

    init {
        launchScope {
            repository.schedule.scheduleWithId(scheduleId).cancellable().collect { schedule ->
                schedule?.let { schedule ->
                    repository.observation.observationById(schedule.observationId).cancellable()
                        .firstOrNull()?.let {
                            _taskDetailsModel.emit(
                                TaskDetailsModel.createModelFrom(
                                    it,
                                    schedule
                                )
                            )
                        }
                }
            }
        }
        launchScope {
            repository.dataPointCount.get(scheduleId).cancellable().collect {
                it?.let {
                    _dataCount.emit(it.count)
                }
            }
        }
        launchScope {
            ObservationStates.observationErrors.collect { errors ->
                _observationErrors.value = errors
                taskDetailsModel.value?.let { taskDetails ->
                    if (taskDetails.observationType != "") {
                        _taskObservationErrors.value = emptyList()
                        _taskObservationErrorActions.value = emptyList()
                        observationErrors.value[taskDetails.observationType]?.let { errors ->
                            val (actions, messages) = errors.partition { it == Observation.ERROR_DEVICE_NOT_CONNECTED }
                            _taskObservationErrors.value = messages.toList()
                            _taskObservationErrorActions.value = actions.toList()
                        }
                    }
                }
            }
        }
    }

    fun onLoadTaskDetails(provideNewState: ((TaskDetailsModel?) -> Unit)): Closeable =
        _taskDetailsModel.asClosure(provideNewState)

    fun onNewDataCount(provideNewState: (Long?) -> Unit) = _dataCount.asClosure(provideNewState)

    fun startObservation() {
        dataRecorder.start(scheduleId)
    }

    fun stopObservation() {
        dataRecorder.stop(scheduleId)
    }

    fun pauseObservation() {
        dataRecorder.pause(scheduleId)
    }
}