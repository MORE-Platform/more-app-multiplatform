package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.filter

import io.redlink.more.more_app_mutliplatform.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.android.extensions.formatDateFilterString
import io.redlink.more.more_app_mutliplatform.android.extensions.formatObservationTypeString
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.models.FilterModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DashboardFilterViewModel(private val coreViewModel: CoreDashboardFilterViewModel) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val observationFactory = AndroidObservationFactory(MoreApplication.appContext!!)

    val currentFilter =  MutableStateFlow(FilterModel())

    val dateFilters = DateFilterModel.values().map {
        Pair(
            it,
            it.toString().formatDateFilterString())
    }

    val typeFilters = observationFactory.observationTypes().map {
        Pair<String, String?>(it.formatObservationTypeString(), it)
    }.toMutableList().apply { this.add(0, Pair("All Items", null)) }.toList()

    init {
        scope.launch {
            coreViewModel.currentFilter.collect {
                    currentFilter.emit(it)
            }
        }
    }

    fun processTypeFilterChange(type: String?) {
        type?.let {
            if(coreViewModel.containsType(it))
                coreViewModel.removeTypeFilter(it)
            else
                coreViewModel.addTypeFilter(it)
        } ?: run{
            coreViewModel.clearTypeFilters()
        }
    }

    fun setDateFilter(dateFilter: DateFilterModel) {
        coreViewModel.setDateFilter(dateFilter)
    }
}