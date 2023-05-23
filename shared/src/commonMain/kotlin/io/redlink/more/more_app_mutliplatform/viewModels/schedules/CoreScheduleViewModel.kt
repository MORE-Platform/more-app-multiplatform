package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleListType
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

class CoreScheduleViewModel(
    private val dataRecorder: DataRecorder,
    private val scheduleListType: ScheduleListType,
    private val coreFilterModel: CoreDashboardFilterViewModel
) : CoreViewModel() {
    private val scheduleRepository = ScheduleRepository()
    private var originalScheduleList = emptySet<ScheduleModel>()

    val scheduleListState = MutableStateFlow(
        Triple(
            emptySet<ScheduleModel>(),
            emptySet<String>(),
            emptySet<ScheduleModel>()
        )
    )

    init {
        launchScope {
            coreFilterModel.currentTypeFilter
                .combine(coreFilterModel.currentDateFilter) { typeFilter, dateFilter ->
                    typeFilter.values.any()
                            && dateFilter[DateFilterModel.ENTIRE_TIME] == false && dateFilter.any { it.value }
                }
                .cancellable().collect {
                    if (it) {
                        updateList(coreFilterModel.applyFilter(originalScheduleList).toSet())
                    } else {
                        val copy = originalScheduleList.toSet()
                        originalScheduleList = emptySet()
                        updateList(copy)
                    }
                }
        }
    }

    override fun viewDidAppear() {
        launchScope {
            scheduleRepository.allSchedulesWithStatus(done = scheduleListType == ScheduleListType.COMPLETED)
                .cancellable()
                .collect {
                    val newList = when (scheduleListType) {
                        ScheduleListType.COMPLETED -> createCompletedModels(it)
                        ScheduleListType.RUNNING -> createRunningModels(it)
                        else -> createModels(it)
                    }
                    val modified = if (coreFilterModel.filterActive()) {
                        coreFilterModel.applyFilter(newList)
                    } else {
                        newList
                    }.toSet()
                    updateList(modified)
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

    private suspend fun updateList(newList: Set<ScheduleModel>) {
        val oldIds = originalScheduleList.map { it.scheduleId }.toSet()
        val newIds = newList.map { it.scheduleId }.toSet()
        val addedIds = newIds - oldIds
        val removedIds = (oldIds - newIds).toMutableSet()
        var added = newList.filter { it.scheduleId in addedIds }.toSet()
        var updated = newList.filter { old ->
            originalScheduleList.any { new -> old.isSameAs(new) && !old.hasSameContentAs(new) }
        }.toSet()

        if (scheduleListType != ScheduleListType.COMPLETED) {
            added = added.filter {
                it.end > Clock.System.now().epochSeconds
                        && it.scheduleState.active()
                        || it.scheduleState == ScheduleState.DEACTIVATED
            }.toSet()
            val (update, remove) = updated.partition {
                it.end > Clock.System.now().epochSeconds
                        && it.scheduleState.active()
                        || it.scheduleState == ScheduleState.DEACTIVATED
            }
            removedIds.addAll(remove.map { it.scheduleId }.toSet())
            updated = update.toSet()
        }

        if (added.isNotEmpty() || removedIds.isNotEmpty() || updated.isNotEmpty()) {
            scheduleListState.emit(Triple(added, removedIds, updated))
        }
        originalScheduleList = newList.toSet()
    }

    private fun createModels(scheduleList: List<ScheduleSchema>): List<ScheduleModel> {
        return scheduleList
            .mapNotNull { ScheduleModel.createModel(it) }
    }

    private fun createCompletedModels(scheduleList: List<ScheduleSchema>): List<ScheduleModel> {
        return scheduleList
            .mapNotNull { ScheduleModel.createModel(it) }
            .filter { scheduleModel -> scheduleModel.scheduleState.completed() }
    }

    private fun createRunningModels(scheduleList: List<ScheduleSchema>): List<ScheduleModel> {
        return createModels(scheduleList).filter { scheduleModel -> scheduleModel.scheduleState.running() }
    }

    fun onScheduleStateUpdated(providedState: (Triple<Set<ScheduleModel>, Set<String>, Set<ScheduleModel>>) -> Unit) =
        scheduleListState.asClosure(providedState)
}

