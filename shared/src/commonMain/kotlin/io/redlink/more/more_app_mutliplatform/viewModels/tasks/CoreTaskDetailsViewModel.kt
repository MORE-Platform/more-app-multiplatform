package io.redlink.more.more_app_mutliplatform.viewModels.tasks

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CoreTaskDetailsViewModel(
    private val scheduleId: String,
    private val dataRecorder: DataRecorder
) {

    private val dataPointCountRepository: DataPointCountRepository = DataPointCountRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    val taskDetailsModel = MutableStateFlow<TaskDetailsModel?>(null)
    val dataCount = MutableStateFlow(0L)

    init {
        scope.launch {
            scheduleRepository.scheduleWithId(scheduleId).collect { schedule ->
                schedule?.let { schedule ->
                    observationRepository.observationById(schedule.observationId).firstOrNull()?.let {
                        taskDetailsModel.value = TaskDetailsModel.createModelFrom(it, schedule)
                    }
                }
            }
        }
        scope.launch {
            dataPointCountRepository.get(scheduleId).collect { countSchema ->
                dataCount.value = countSchema?.count ?: 0
            }
        }
    }

    fun onLoadTaskDetails(provideNewState: ((TaskDetailsModel?) -> Unit)): Closeable {
        return taskDetailsModel.asClosure(provideNewState)
    }

    fun onNewDataCount(provideNewState: (Long?) -> Unit) = dataCount.asClosure(provideNewState)

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