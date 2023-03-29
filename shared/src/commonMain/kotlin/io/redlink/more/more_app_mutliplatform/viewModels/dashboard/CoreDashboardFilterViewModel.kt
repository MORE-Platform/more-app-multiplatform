package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.FilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import kotlinx.datetime.*

class CoreDashboardFilterViewModel {
    val currentFilter = FilterModel()

    fun addTypeFilter(type: String) = currentFilter.observationTypeFilter.add(type)

    fun removeTypeFilter(type: String) = currentFilter.observationTypeFilter.remove(type)

    fun clearTypeFilters() = currentFilter.observationTypeFilter.clear()

    fun setDateFilter(dateFilter: DateFilterModel) {
        currentFilter.dateFilter = dateFilter
    }

    fun applyFilter(scheduleModelList: Map<LocalDate, List<ScheduleModel>>): Map<LocalDate, List<ScheduleModel>> {
        val filteredMap = scheduleModelList.filterKeys { date ->
            currentFilter.dateFilter?.let {
                date <= Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(it.getDuration()) }
                ?: true
        }.toMutableMap()

        if(currentFilter.hasObservationTypeFilter()){
            filteredMap.forEach { (key, value) ->
                filteredMap[key] = value.filter {
                    currentFilter.observationTypeFilter.contains(it.observationType)
                }
                if(filteredMap[key]?.isEmpty() == true)
                    filteredMap.remove(key)
            }
        }
        return filteredMap
    }
}