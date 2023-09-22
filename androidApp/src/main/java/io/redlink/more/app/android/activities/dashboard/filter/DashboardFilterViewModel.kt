package io.redlink.more.app.android.activities.dashboard.filter

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.formatDateFilterString
import io.redlink.more.app.android.extensions.getQuantityString
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardFilterViewModel(private val coreViewModel: CoreDashboardFilterViewModel) {
    val currentTypeFilter = mutableStateMapOf<String,Boolean>()
    val currentDateFilter = mutableStateMapOf<DateFilterModel, Boolean>()

    val dateFilters = DateFilterModel.values().associateWith { it.toString().formatDateFilterString() }

    val typeFilterActive: MutableState<Boolean> = mutableStateOf(coreViewModel.activeTypeFilter())

    init {
        launch {
            coreViewModel.currentTypeFilter.collect {
                withContext(Dispatchers.Main) {
                    currentTypeFilter.putAll(it)
                    typeFilterActive.value = coreViewModel.activeTypeFilter()
                }
            }
        }
        launch {
            coreViewModel.currentDateFilter.collect {
                withContext(Dispatchers.Main) {
                    currentDateFilter.putAll(it)
                }
            }
        }
    }

    fun toggleTypeFilter(type: String) {
        coreViewModel.toggleTypeFilter(type)
    }

    fun clearTypeFilter() {
        coreViewModel.clearTypeFilters()
    }

    fun toggleDateFilter(dateFilter: DateFilterModel) {
        coreViewModel.toggleDateFilter(dateFilter)
    }

    fun getFilterString(): String {
        var filterString = ""
        val typesAmount = coreViewModel.currentTypeFilter.value.filter { it.value }.size
        val dateFilter = coreViewModel.currentDateFilter.value.filterValues { it }.keys.firstOrNull() ?: ""

        if (coreViewModel.filterActive()) {
            if(coreViewModel.activeDateFilter()) {
                filterString += dateFilter
            }

            if(coreViewModel.activeTypeFilter()) {
                if(filterString.isNotBlank())
                    filterString += ", "
                filterString += getQuantityString(R.plurals.filter_text, typesAmount, typesAmount)
            }
        } else {
            filterString += stringResource(R.string.more_filter_deactivated)
        }

        return filterString
    }
}