package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDate
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.FilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        Napier.i { "Current Filters: ${currentFilter.value}" }
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
        Napier.i { "Cleared filters: ${currentFilter.value}" }
    }

    fun setDateFilter(dateFilter: DateFilterModel) {
        update(currentFilter.value.copy(dateFilter = dateFilter))
    }

    fun hasDateFilter(dateFilter: DateFilterModel) = currentFilter.value.dateFilter == dateFilter

    fun applyFilter(scheduleModelList: Map<Long, List<ScheduleModel>>): Map<Long, List<ScheduleModel>> {
        val scheduleListAsLocalDate = scheduleModelList.mapKeys {
            it.key.toLocalDate()
        }.toMutableMap()

        if (currentFilter.value.typeFilter.isNotEmpty()) {
            scheduleListAsLocalDate.forEach { (key, value) ->
                scheduleListAsLocalDate[key] = value.filter {
                    currentFilter.value.typeFilter.contains(it.observationType)
                }
            }
        }

        val filteredMap = scheduleListAsLocalDate.filterKeys { date ->
            currentFilter.value.dateFilter.duration?.let {
                date <= Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(it)
            } ?: true
                    && scheduleListAsLocalDate[date]?.isNotEmpty()
                    ?: false
        }
        return filteredMap.mapKeys {
            it.key.time()
        }
    }

    private fun update(newFilterModel: FilterModel) {
        scope.launch {
            currentFilter.emit(newFilterModel)
        }
    }

    fun onLoadCurrentFilters(provideNewState: ((FilterModel) -> Unit)): Closeable {
        val job = Job()
        currentFilter.onEach {
            Napier.i { "New Filter Model: ${currentFilter.value}" }
            provideNewState(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))
        return object : Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }
}