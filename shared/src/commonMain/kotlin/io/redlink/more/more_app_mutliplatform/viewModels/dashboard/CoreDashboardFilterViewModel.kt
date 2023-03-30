package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.redlink.more.more_app_mutliplatform.extensions.time
import io.redlink.more.more_app_mutliplatform.extensions.toLocalDate
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.models.TypeFilterModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class CoreDashboardFilterViewModel {
    val currentDateFilter = MutableStateFlow(DateFilterModel.ENTIRE_TIME)
    val currentTypeFilter = MutableStateFlow(mutableSetOf<TypeFilterModel>())

    fun hasAllTypes() = currentTypeFilter.value.isEmpty()

    fun containsTypeFilter(type: TypeFilterModel) = currentTypeFilter.value.contains(type)

    fun addTypeFilter(type: TypeFilterModel) = currentTypeFilter.value.add(type)

    fun removeTypeFilter(type: TypeFilterModel) = currentTypeFilter.value.remove(type)

    fun clearTypeFilters() = currentTypeFilter.value.clear()

    fun hasDateFilter(dateFilter: DateFilterModel) = currentDateFilter.value == dateFilter

    fun setDateFilter(dateFilter: DateFilterModel) {
        currentDateFilter.value = dateFilter
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
                    currentTypeFilter.value.map{ typeFilterModel ->
                        typeFilterModel.type
                    }.contains(it.observationType)
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