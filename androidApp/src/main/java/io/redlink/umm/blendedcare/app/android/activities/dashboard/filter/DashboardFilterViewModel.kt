/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.umm.blendedcare.app.android.activities.dashboard.filter

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.extensions.formatDateFilterString
import io.redlink.umm.blendedcare.app.android.extensions.getQuantityString
import io.redlink.umm.blendedcare.app.android.extensions.stringResource
import io.redlink.umm.participant.models.DateFilterModel
import io.redlink.umm.participant.scopes.Scope.launch
import io.redlink.umm.participant.viewModels.dashboard.CoreDashboardFilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardFilterViewModel(private val coreViewModel: CoreDashboardFilterViewModel) :
    ViewModel() {
    val currentTypeFilter = mutableStateMapOf<String, Boolean>()
    val currentDateFilter = mutableStateMapOf<DateFilterModel, Boolean>()

    val dateFilters =
        DateFilterModel.entries.associateWith { it.toString().formatDateFilterString() }

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
        val dateFilter =
            coreViewModel.currentDateFilter.value.filterValues { it }.keys.firstOrNull() ?: ""

        if (coreViewModel.filterActive()) {
            if (coreViewModel.activeDateFilter()) {
                filterString += dateFilter
            }

            if (coreViewModel.activeTypeFilter()) {
                if (filterString.isNotBlank())
                    filterString += ", "
                filterString += getQuantityString(R.plurals.filter_text, typesAmount, typesAmount)
            }
        } else {
            filterString += stringResource(R.string.more_filter_deactivated)
        }

        return filterString
    }
}