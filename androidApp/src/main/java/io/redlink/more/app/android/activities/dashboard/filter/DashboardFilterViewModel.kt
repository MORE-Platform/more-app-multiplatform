package io.redlink.more.app.android.activities.dashboard.filter

import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.formatDateFilterString
import io.redlink.more.app.android.extensions.formatObservationTypeString
import io.redlink.more.app.android.extensions.getQuantityString
import io.redlink.more.app.android.extensions.getString
import io.redlink.more.app.android.observations.AndroidObservationFactory
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

    fun getFilterString(): String {
        var filterString = ""
        val typesAmount = coreViewModel.currentFilter.value.typeFilter.size
        val dateFilter = coreViewModel.currentFilter.value.dateFilter.toString().formatDateFilterString()

        if(!coreViewModel.hasDateFilter(DateFilterModel.ENTIRE_TIME))
            filterString += dateFilter

        if(!coreViewModel.hasAllTypes()) {
            if(filterString.isNotBlank())
                filterString += ", "
            filterString += getQuantityString(R.plurals.filter_text, typesAmount, typesAmount)
        }

        if(filterString.isBlank())
            filterString += getString(R.string.more_filter_deactivated)

        return filterString
    }
}