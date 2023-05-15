package io.redlink.more.more_app_mutliplatform.viewModels.tasks

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull

class CoreTaskDetailsViewModel(
    private val dataRecorder: DataRecorder
): CoreViewModel() {
    private var scheduleId: String? = null

    private val dataPointCountRepository: DataPointCountRepository = DataPointCountRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    val taskDetailsModel = MutableStateFlow<TaskDetailsModel?>(null)
    val dataCount = MutableStateFlow<Long>(0)

    private var countJob: Job? = null

    fun setSchedule(scheduleId: String) {
        this.scheduleId = scheduleId
    }

    override fun viewDidAppear() {
        scheduleId?.let { scheduleId ->
            launchScope {
                scheduleRepository.scheduleWithId(scheduleId).cancellable().collect { schedule ->
                    schedule?.let { schedule ->
                        observationRepository.observationById(schedule.observationId).cancellable().firstOrNull()?.let {
                            taskDetailsModel.emit(TaskDetailsModel.createModelFrom(it, schedule))
                        }
                    }
                }
            }
            launchScope {
                dataPointCountRepository.get(scheduleId).cancellable().collect {
                    it?.let { dataCount.emit(it.count) }
                }
            }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        this.scheduleId = null
        this.countJob?.cancel()
        launchScope {
            taskDetailsModel.emit(null)
            dataCount.emit(0)
        }
    }

    fun onLoadTaskDetails(provideNewState: ((TaskDetailsModel?) -> Unit)): Closeable {
        return taskDetailsModel.asClosure(provideNewState)
    }

    fun onNewDataCount(provideNewState: (Long?) -> Unit) = dataCount.asClosure(provideNewState)

    fun startObservation() {
        scheduleId?.let { dataRecorder.start(it) }
    }

    fun stopObservation() {
        scheduleId?.let { dataRecorder.stop(it) }
    }

    fun pauseObservation() {
        scheduleId?.let { dataRecorder.pause(it) }
    }
}