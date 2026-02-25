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
package io.redlink.more.app.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.EmptyListView
import io.redlink.more.app.android.shared_composables.ScheduleList
import io.redlink.umm.blendedcare.app.android.R

@Composable
fun ScheduleListView(
    navController: NavController,
    scheduleViewModel: ScheduleViewModel,
    showButton: Boolean
) {
    val scheduleList by scheduleViewModel.coreViewModel.schedulesByDate.collectAsStateWithLifecycle()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (scheduleList.isNotEmpty()) {
            ScheduleList(
                navController = navController,
                viewModel = scheduleViewModel,
                showButton = showButton
            )
        } else {
            EmptyListView(getStringResource(R.string.more_schedule_empty_list))
        }
    }
}