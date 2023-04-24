package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.FilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class CoreDashboardFilterViewModel {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    val currentFilter = MutableStateFlow(FilterModel())

    fun hasAllTypes() = currentFilter.value.typeFilter.isEmpty()

    fun containsType(type: String) = currentFilter.value.typeFilter.contains(type)

    fun addTypeFilter(type: String) {
        update(currentFilter.value.copy(
            typeFilter = currentFilter.value.typeFilter
                .toMutableSet().apply { add(type) }
        ))
    }

    fun getEnumAsList(): List<DateFilterModel> {
        return DateFilterModel.values().toList()
    }

    fun removeTypeFilter(type: String) {
        update(currentFilter.value.copy(
            typeFilter = currentFilter.value.typeFilter
                .toMutableSet().apply { remove(type) }
        ))
    }

    fun clearTypeFilters() {
        update(currentFilter.value.copy(
            typeFilter = currentFilter.value.typeFilter
                .toMutableSet().apply { clear() }
        ))
    }

    fun setDateFilter(dateFilter: DateFilterModel) {
        update(currentFilter.value.copy(dateFilter = dateFilter))
    }

    fun setTypeFilters(filters: List<String>) {
        update(currentFilter.value.copy(
            typeFilter = currentFilter.value.typeFilter
                .toMutableSet().apply {
                    clear()
                    addAll(filters)
                }
        ))
    }

    fun hasDateFilter(dateFilter: DateFilterModel) = currentFilter.value.dateFilter == dateFilter

    fun applyFilter(scheduleModelList: Map<Long, List<ScheduleModel>>): Map<Long, List<ScheduleModel>> {
        var schedules = scheduleModelList

        if (currentFilter.value.typeFilter.isNotEmpty()) {
            schedules = schedules.mapValues {
                it.value.filter { schedule ->
                    currentFilter.value.typeFilter.contains(schedule.observationType)
                }
            }
        }
        return schedules.filterKeys { date ->
            currentFilter.value.dateFilter.duration?.let {
                date <= Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(it).time()
            } ?: true
                    && schedules[date]?.isNotEmpty() ?: false
        }
    }
    private fun update(newFilterModel: FilterModel) {
        scope.launch {
            currentFilter.emit(newFilterModel)
        }
    }

    fun onLoadCurrentFilters(provideNewState: ((FilterModel) -> Unit)): Closeable {
        return currentFilter.asClosure(provideNewState)
    }
}