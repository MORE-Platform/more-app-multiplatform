package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun DatapointCollectionView (datapoints: MutableState<Long>){

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f)
            .padding(8.dp)
    ){
        CircularProgressIndicator(
            color = MoreColors.Approved,
            modifier = Modifier
                .height(50.dp)
                .width(50.dp))
        MediumTitle(text = getStringResource(id = R.string.more_observation_datapoints))
        Text(
            text = datapoints.value.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoreColors.Secondary)
    }
}