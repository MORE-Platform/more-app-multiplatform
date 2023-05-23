package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.DateFilter
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

class CoreDashboardFilterViewModel {
    val currentTypeFilter = MutableStateFlow(emptyMap<String, Boolean>())
    val currentDateFilter = MutableStateFlow(
        DateFilterModel.values().associateWith { it == DateFilterModel.ENTIRE_TIME })

    fun hasAnyTypes() = currentTypeFilter.value.values.any()

    fun addTypes(observationFactory: ObservationFactory) {
        currentTypeFilter.set(observationFactory.observationTypes().associateWith { false })
    }

    fun toggleTypeFilter(type: String) {
        val typeFilter = currentTypeFilter.value.toMutableMap()
        typeFilter[type] = !typeFilter.getOrElse(type) { false }
        currentTypeFilter.set(typeFilter)
    }

    fun clearTypeFilters() {
        currentTypeFilter.set(currentTypeFilter.value.keys.associateWith { false })
    }

    fun activeTypeFilter() = currentTypeFilter.value.any { it.value }

    fun toggleDateFilter(date: DateFilter) {
        date.toEnum()?.let { toggleDateFilter(it) }
    }

    fun toggleDateFilter(date: DateFilterModel) {
        val dateFilter = currentDateFilter.value.toMutableMap()
        dateFilter[date]?.let { set ->
            if (!set) {
                dateFilter.keys.forEach { dateFilter[it] = false }
            }
            dateFilter[date] = !set
            if (dateFilter.all { !it.value }) {
                dateFilter[DateFilterModel.ENTIRE_TIME] = true
            }
        }
        currentDateFilter.set(dateFilter)
    }

    fun hasDateFilter(dateFilter: DateFilterModel) =
        currentDateFilter.value.getOrElse(dateFilter) { false }

    private fun activeDateFilter() =
        currentDateFilter.value[DateFilterModel.ENTIRE_TIME] == false && currentDateFilter.value.any { it.value }

    fun filterActive() = activeDateFilter() || activeTypeFilter()

    fun applyFilter(scheduleModelList: Collection<ScheduleModel>): Collection<ScheduleModel> {
        var schedules = scheduleModelList
        if (filterActive()) {
            if (activeTypeFilter()) {
                val activeTypes = currentTypeFilter.value.filterValues { it }.keys
                schedules = schedules.filter { schedule ->
                    schedule.observationType in activeTypes
                }
            }
            if (activeDateFilter()) {
                currentDateFilter.value.filterValues { it }.keys.firstOrNull()
                    ?.let { dateFilter ->
                        val now = Clock.System.now()
                        val until = now.plus(
                            dateFilter.number,
                            dateFilter.dateBased!!,
                            TimeZone.currentSystemDefault()
                        ).epochSeconds
                        schedules = schedules.filter { it.start <= until }
                    }
            }
        }
        return schedules
    }

    fun onNewTypeFilter(provideNewState: (Map<String, Boolean>) -> Unit) = currentTypeFilter.asClosure(provideNewState)

    fun onNewDateFilter(provideNewState: (Map<DateFilter, Boolean>) -> Unit) = currentDateFilter.transform { emit(it.mapKeys { it.key.asDataClass() }) }.asClosure(provideNewState)
}