package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDate
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class CoreDashboardFilterViewModel {
    val currentDateFilter = MutableStateFlow(DateFilterModel.ENTIRE_TIME)
    val currentTypeFilter = MutableStateFlow(mutableSetOf<String>())

    fun hasAllTypes() = currentTypeFilter.value.isEmpty()

    fun containsTypeFilter(type: String) = currentTypeFilter.value.contains(type)

    fun addTypeFilter(type: String) {
        val copy = currentTypeFilter.value.toMutableSet()
        copy.add(type)
        currentTypeFilter.update {
            copy
        }
    }

    fun removeTypeFilter(type: String) {
        val copy = currentTypeFilter.value.toMutableSet()
        copy.remove(type)
        currentTypeFilter.update {
            copy
        }
    }

    fun clearTypeFilters() {
        val copy = currentTypeFilter.value.toMutableSet()
        copy.clear()
        currentTypeFilter.update {
            copy
        }
    }

    fun hasDateFilter(dateFilter: DateFilterModel) = currentDateFilter.value == dateFilter

    fun setDateFilter(dateFilter: DateFilterModel) {
        currentDateFilter.update { dateFilter }
    }

    fun applyFilter(scheduleModelList: Map<Long, List<ScheduleModel>>): Map<Long, List<ScheduleModel>> {
        val scheduleModelListAsLocalDate = scheduleModelList.mapKeys {
            it.key.toLocalDate()
        }

        val filteredMap = scheduleModelListAsLocalDate.filterKeys { date ->
            currentDateFilter.value.duration?.let {
                date <= Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(it) }
                ?: true
        }.toMutableMap()

        if(currentTypeFilter.value.isNotEmpty()){
            filteredMap.forEach { (key, value) ->
                filteredMap[key] = value.filter {
                    currentTypeFilter.value.contains(it.observationType)
                }
                if(filteredMap[key]?.isEmpty() == true)
                    filteredMap.remove(key)
            }
        }
        return filteredMap.mapKeys {
            it.key.time()
        }
    }
}