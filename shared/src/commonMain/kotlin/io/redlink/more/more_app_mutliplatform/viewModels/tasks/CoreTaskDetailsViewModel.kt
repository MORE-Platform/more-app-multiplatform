package io.redlink.more.more_app_mutliplatform.viewModels.tasks

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CoreTaskDetailsViewModel(
    private val scheduleId: String,
    private val dataRecorder: DataRecorder
) {

    private val dataPointCountRepository: DataPointCountRepository = DataPointCountRepository(RealmDatabase)
    private val observationRepository: ObservationRepository = ObservationRepository()
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    val taskDetailsModel = MutableStateFlow<TaskDetailsModel?>(null)

    init {
        scope.launch {
            scheduleRepository.scheduleWithId(scheduleId).collect { schedule ->
                schedule?.let { schedule ->
                    observationRepository.observationById(schedule.observationId).firstOrNull()?.let {
                        val count = dataPointCountRepository.get(schedule.scheduleId.toHexString()).firstOrNull()
                        taskDetailsModel.emit(TaskDetailsModel.createModelFrom(it, schedule, count?.count ?: 0))
                    }
                }
            }
        }
        scope.launch {
            dataPointCountRepository.get(scheduleId).collect { countSchema ->
                countSchema?.let {
                    taskDetailsModel.firstOrNull()?.let { model ->
                        model.dataPointCount = it.count
                        taskDetailsModel.emit(model)
                    }
                }
            }
        }
    }

    fun onLoadTaskDetails(provideNewState: ((TaskDetailsModel?) -> Unit)): Closeable {
        return taskDetailsModel.asClosure(provideNewState)
    }

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