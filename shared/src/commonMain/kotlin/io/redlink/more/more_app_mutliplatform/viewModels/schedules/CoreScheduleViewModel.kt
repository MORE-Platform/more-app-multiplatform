package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDate
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

class CoreScheduleViewModel(
    private val dataRecorder: DataRecorder,
    private val scheduleListType: ScheduleListType,
    private val coreFilterModel: CoreDashboardFilterViewModel
) : CoreViewModel() {
    private val observationRepository = ObservationRepository()
    private val scheduleRepository = ScheduleRepository()

    val scheduleList: MutableStateFlow<List<ScheduleModel>> = MutableStateFlow(emptyList())
    val scheduleDates: MutableStateFlow<Set<Long>> = MutableStateFlow(emptySet())

    private var originalScheduleList = emptyList<ScheduleModel>()

    override fun viewDidAppear() {
        launchScope {
            coreFilterModel.currentFilter.collect {
                if (coreFilterModel.filterActive()) {
                    if (originalScheduleList.isEmpty()) {
                        originalScheduleList = scheduleList.value
                    }
                    updateList(coreFilterModel.applyFilter(originalScheduleList))
                } else {
                    updateList(originalScheduleList)
                    originalScheduleList = emptyList()
                }
            }
        }
        launchScope {
            scheduleRepository.allSchedulesWithStatus(done = scheduleListType == ScheduleListType.COMPLETED)
                .combine(observationRepository.observations()) { schedules, observations ->
                    observations.associate { observation ->
                        observation.observationTitle to schedules
                            .filter { it.observationId == observation.observationId }
                    }
                }.collect {
                    val newList = when (scheduleListType) {
                        ScheduleListType.COMPLETED -> createCompletedMap(it)
                        ScheduleListType.RUNNING -> createRunningMap(it)
                        else -> createMap(it)
                    }

                    if (coreFilterModel.filterActive()) {
                        originalScheduleList = newList
                        updateList(coreFilterModel.applyFilter(newList))
                    } else {
                        updateList(newList)
                    }
                }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
    }

    fun start(scheduleId: String) {
        dataRecorder.start(scheduleId)
    }

    fun pause(scheduleId: String) {
        dataRecorder.pause(scheduleId)
    }

    fun stop(scheduleId: String) {
        dataRecorder.stop(scheduleId)
    }

    private suspend fun updateList(list: List<ScheduleModel>) {
        scheduleList.emit(list)
        scheduleDates.emit(list.map { it.start.toLocalDate().time() }.toSet())
    }

    private fun createMap(observationList: Map<String, List<ScheduleSchema>>): List<ScheduleModel> {
        return observationList
            .flatMap { ScheduleModel.createModelsFrom(it.key, it.value) }
            .filter {
                it.end > Clock.System.now().toEpochMilliseconds()
                        && it.scheduleState.active()
                        || it.scheduleState == ScheduleState.DEACTIVATED
            }
    }

    private fun createCompletedMap(observationList: Map<String, List<ScheduleSchema>>): List<ScheduleModel> {
        return observationList
            .flatMap { ScheduleModel.createModelsFrom(it.key, it.value) }
            .filter { scheduleModel -> scheduleModel.scheduleState.completed() }
    }

    private fun createRunningMap(observationList: Map<String, List<ScheduleSchema>>): List<ScheduleModel> {
        return createMap(observationList).filter { scheduleModel -> scheduleModel.scheduleState.running() }
    }

    fun onScheduleModelListChange(provideNewState: (List<ScheduleModel>) -> Unit): Closeable {
        return scheduleList.asClosure(provideNewState)
    }

    fun scheduleDateListChange(provideNewState: (Set<Long>) -> Unit) = scheduleDates.asClosure(provideNewState)
}

