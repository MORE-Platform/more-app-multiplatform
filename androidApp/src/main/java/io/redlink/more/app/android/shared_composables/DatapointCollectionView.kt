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
package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.models.ScheduleState


@Composable
fun DatapointCollectionView (datapoints: Long, scheduleState: ScheduleState?){

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ){
        if(scheduleState == ScheduleState.RUNNING) {
            CircularProgressIndicator(
                color = MoreColors.Approved,
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
            )
            Spacer(Modifier.padding(6.dp))
        }

        if(scheduleState == ScheduleState.RUNNING || datapoints > 0) {
            MediumTitle(text = getStringResource(id = R.string.more_observation_datapoints))
            Spacer(Modifier.padding(4.dp))
            Text(
                text = datapoints.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MoreColors.Secondary
            )
        }
    }
}