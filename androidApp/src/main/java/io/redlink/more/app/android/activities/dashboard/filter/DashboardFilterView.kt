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
package io.redlink.more.app.android.activities.dashboard.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.shared_composables.HeaderDescription
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.shared_composables.IconInline
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.ui.theme.MoreColors


@Composable
fun DashboardFilterView(viewModel: DashboardFilterViewModel) {
    LazyColumn {
        item {
            HeaderTitle(
                title = stringResource(R.string.more_filter_set_duration),
                modifier = Modifier.padding(top = 20.dp)
            )
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        itemsIndexed(viewModel.currentDateFilter.entries.sortedBy { it.key.sortIndex }) { _, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (item.value)
                    IconInline(
                        icon = Icons.Rounded.Done,
                        color = MoreColors.Approved,
                        contentDescription = getStringResource(id = R.string.more_filter_selected)
                    )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clickable(onClick = { viewModel.toggleDateFilter(item.key) })
                        .padding(4.dp)
                ) {
                    HeaderDescription(
                        description = viewModel.dateFilters[item.key]
                            ?: getStringResource(id = R.string.more_filter_all),
                        color = if (item.value) MoreColors.Primary else MoreColors.Secondary
                    )
                }
            }
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        item {
            HeaderTitle(
                title = stringResource(R.string.more_filter_set_type),
                modifier = Modifier.padding(top = 20.dp)
            )
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!viewModel.typeFilterActive.value)
                    IconInline(
                        icon = Icons.Rounded.Done,
                        color = MoreColors.Approved,
                        contentDescription = getStringResource(id = R.string.more_filter_selected)
                    )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clickable(onClick = { viewModel.clearTypeFilter() })
                        .padding(4.dp)
                ) {
                    HeaderDescription(
                        description = stringResource(R.string.more_filter_all),
                        color = if (!viewModel.typeFilterActive.value) MoreColors.Primary else MoreColors.Secondary
                    )
                }
            }
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }

        items(viewModel.currentTypeFilter.entries.sortedBy { it.key }) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (
                    item.value
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
                        .clickable(onClick = { viewModel.toggleTypeFilter(item.key) })
                        .padding(4.dp)
                ) {
                    HeaderDescription(
                        description = item.key,
                        color = if (item.value) MoreColors.Primary else MoreColors.Secondary
                    )
                }
            }
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }
    }
}