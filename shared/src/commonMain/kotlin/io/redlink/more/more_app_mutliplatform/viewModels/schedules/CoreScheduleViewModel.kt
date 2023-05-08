package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.localDateTime
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CoreScheduleViewModel(
    private val dataRecorder: DataRecorder,
    private val scheduleListType: ScheduleListType,
    private val coreFilterModel: CoreDashboardFilterViewModel
) : CoreViewModel() {
    private val observationRepository = ObservationRepository()
    private val scheduleRepository = ScheduleRepository()

    val scheduleModelList: MutableStateFlow<Map<Long, List<ScheduleModel>>> =
        MutableStateFlow(emptyMap())
    private val originalScheduleList = mutableMapOf<Long, List<ScheduleModel>>()

    override fun viewDidAppear() {
        launchScope {
            scheduleRepository.allSchedulesWithStatus(done = scheduleListType == ScheduleListType.COMPLETED)
                .combine(observationRepository.observations()) { schedules, observations ->
                    observations.associate { observation ->
                        observation.observationTitle to schedules
                            .filter { it.observationId == observation.observationId }
                    }
                }.collect {
                    val newMap = when (scheduleListType) {
                        ScheduleListType.COMPLETED -> createCompletedMap(it)
                        ScheduleListType.RUNNING -> createRunningMap(it)
                        else -> createMap(it)
                    }
                    if (coreFilterModel.filterActive()) {
                        originalScheduleList.clear()
                        originalScheduleList.putAll(newMap)
                        scheduleModelList.emit(coreFilterModel.applyFilter(originalScheduleList))
                    } else {
                        scheduleModelList.emit(newMap)
                    }
                }
        }
        launchScope {
            coreFilterModel.currentFilter.collect {
                if (coreFilterModel.filterActive()) {
                    if (originalScheduleList.isEmpty()) {
                        originalScheduleList.putAll(scheduleModelList.value.toMap())
                    }
                    scheduleModelList.emit(coreFilterModel.applyFilter(originalScheduleList))
                } else {
                    scheduleModelList.emit(originalScheduleList)
                    originalScheduleList.clear()
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

    private fun createMap(observationList: Map<String, List<ScheduleSchema>>): Map<Long, List<ScheduleModel>> {
        return observationList
            .asSequence()
            .map { ScheduleModel.createModelsFrom(it.key, it.value) }
            .flatten()
            .filter {
                it.end > Clock.System.now().toEpochMilliseconds()
                        && !it.done
                        && it.scheduleState.active()
                        || it.scheduleState == ScheduleState.DEACTIVATED
            }
            .sortedBy { it.start }
            .groupBy {
                if (it.start <= Clock.System.now().toEpochMilliseconds()) {
                    Clock.System.now().localDateTime().date.time()
                } else {
                    it.start.toLocalDate().time()
                }
            }
            .filterKeys { it >= Clock.System.now().localDateTime().date.time() }
            .filterValues { it.isNotEmpty() }
    }

    private fun createCompletedMap(observationList: Map<String, List<ScheduleSchema>>): Map<Long, List<ScheduleModel>> {
        return observationList
            .flatMap { ScheduleModel.createModelsFrom(it.key, it.value) }
            .filter { scheduleModel -> scheduleModel.scheduleState.completed() }
            .sortedBy { it.start }
            .groupBy {
                it.start.toLocalDate().time()
            }.filterValues { it.isNotEmpty() }
    }

    private fun createRunningMap(observationList: Map<String, List<ScheduleSchema>>): Map<Long, List<ScheduleModel>> {
        return createMap(observationList).mapValues {
            it.value.filter { scheduleModel ->
                scheduleModel.scheduleState == ScheduleState.RUNNING || scheduleModel.scheduleState == ScheduleState.PAUSED
            }
        }.filterValues { it.isNotEmpty() }
    }

    fun onScheduleModelListChange(provideNewState: (Map<Long, List<ScheduleModel>>) -> Unit): Closeable {
        return scheduleModelList.asClosure(provideNewState)
    }


}

