package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.HeaderDescription
import io.redlink.more.more_app_mutliplatform.android.shared_composables.HeaderTitle
import io.redlink.more.more_app_mutliplatform.android.shared_composables.IconInline
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel

@Composable
fun DashboardFilterView(viewModel: DashboardFilterViewModel) {

    val onChangeStateDate: (DateFilterModel) -> Unit = {
        viewModel.setDateFilter(it)
    }

    val onChangeStateType: (String?) -> Unit = {
        viewModel.processTypeFilterChange(it)
    }


    LazyColumn{
        item{
            HeaderTitle(
                title = getString(R.string.more_filter_set_duration),
                modifier = Modifier.padding(top = 20.dp)
            )
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        items(viewModel.dateFilters) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if(viewModel.currentFilter.collectAsState().value.dateFilter == item.first)
                    IconInline(
                        icon = Icons.Rounded.Done,
                        color = MoreColors.Approved,
                        contentDescription = getStringResource(id = R.string.more_filter_selected)
                    )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clickable(onClick = { onChangeStateDate(item.first) })
                        .padding(4.dp)
                ) {
                    HeaderDescription(
                        description = item.second,
                        color = MoreColors.Secondary
                    )
                }
            }
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        item{
            HeaderTitle(
                title = getString(R.string.more_filter_set_type),
                modifier = Modifier.padding(top = 20.dp)
            )
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        items(viewModel.typeFilters.toList()) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if(
                    item.second?.let { viewModel.currentFilter.collectAsState().value.typeFilter.contains(it) }
                        ?: viewModel.currentFilter.collectAsState().value.typeFilter.isEmpty()
                )
                    IconInline(
                        icon = Icons.Rounded.Done,
                        color = MoreColors.Approved,
                        contentDescription = getStringResource(id = R.string.more_filter_selected)
                    )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clickable(onClick = { onChangeStateType(item.second) })
                        .padding(4.dp)
                ) {
                    HeaderDescription(
                        description = item.first,
                        color = MoreColors.Secondary
                    )
                }
            }
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }
    }
}