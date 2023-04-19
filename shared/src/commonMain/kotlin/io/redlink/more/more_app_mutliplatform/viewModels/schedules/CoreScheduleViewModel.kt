package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.localDateTime
import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDate
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CoreScheduleViewModel(
    private val dataRecorder: DataRecorder,
    private val coreFilterModel: CoreDashboardFilterViewModel
    ) {
    private val observationRepository = ObservationRepository()
    private val scheduleRepository = ScheduleRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    val scheduleModelList: MutableStateFlow<Map<Long, List<ScheduleModel>>> =
        MutableStateFlow( emptyMap() )
    private val originalScheduleList = mutableMapOf<Long, List<ScheduleModel>>()

    init {
        scope.launch {
            scheduleRepository.allSchedulesWithStatus()
                .combine(observationRepository.observations()){ schedules, observations ->
                    observations.associate { observation ->
                        observation.observationTitle to schedules
                            .filter { it.observationId == observation.observationId } }
                }.collect {
                    originalScheduleList.clear()
                    originalScheduleList.putAll(createMap(it))
                    scheduleModelList.emit(coreFilterModel.applyFilter(originalScheduleList))

                }
        }

        scope.launch {
            coreFilterModel.currentFilter.collect {
                scheduleModelList.emit(coreFilterModel.applyFilter(originalScheduleList))
            }
        }
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
                if(it.start <= Clock.System.now().toEpochMilliseconds()) {
                    Clock.System.now().localDateTime().date.time()
                }
                else { it.start.toLocalDate().time() }
            }
            .filterKeys { it >= Clock.System.now().localDateTime().date.time() }
            .filterValues { it.isNotEmpty() }
    }

    fun onScheduleModelListChange(provideNewState: (Map<Long, List<ScheduleModel>>) -> Unit): Closeable {
        return scheduleModelList.asClosure(provideNewState)
    }
}

