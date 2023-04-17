package io.redlink.more.app.android.shared_composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.app.android.R


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