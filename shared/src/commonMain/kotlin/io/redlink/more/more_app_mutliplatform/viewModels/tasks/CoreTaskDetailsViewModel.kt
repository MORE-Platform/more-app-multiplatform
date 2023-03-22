package io.redlink.more.more_app_mutliplatform.viewModels.tasks

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CoreTaskDetailsViewModel(private val dataRecorder: DataRecorder) {

    private val dataPointCountRepository: DataPointCountRepository = DataPointCountRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private var dataPointCount: MutableStateFlow<DataPointCountSchema?> = MutableStateFlow(null)
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    val taskDetailsModel: MutableStateFlow<TaskDetailsModel?> = MutableStateFlow(null)

    fun loadTaskDetails(observationId: String, scheduleId: String) {
        scope.launch {
            observationRepository.getObservationByObservationId(observationId)?.let { observation ->
                scheduleRepository.scheduleWithId(scheduleId).collect {
                    it?.let { schedule ->
                        dataPointCountRepository.get(schedule.scheduleId.toHexString()).collect { count ->
                            taskDetailsModel.value = TaskDetailsModel.createModelFrom(observation, schedule, count)
                        }
                    }
                }
            }
            taskDetailsModel.collect {
                it?.let {
                    dataPointCount.value = it.dataPointCount
                }
            }
        }
    }

    fun onLoadTaskDetails(observationId: String, scheduleId: String, provideNewState: ((TaskDetailsModel?) -> Unit)): Closeable {
        loadTaskDetails(observationId, scheduleId)
        return taskDetailsModel.asClosure(provideNewState)
    }

    fun onLoadDataPointCount(provideNewState: ((DataPointCountSchema?) -> Unit)): Closeable {
        return dataPointCount.asClosure(provideNewState)
    }

    fun startObservation(scheduleId: String) {
        dataRecorder.start(scheduleId)
    }

    fun stopObservation(scheduleId: String) {
        dataRecorder.stop(scheduleId)
    }

    fun pauseObservation(scheduleId: String) {
        dataRecorder.stop(scheduleId)
    }
}