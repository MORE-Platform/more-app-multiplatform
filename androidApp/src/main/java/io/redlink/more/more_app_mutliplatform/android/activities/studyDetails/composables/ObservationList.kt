package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.shared_composables.BasicText
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MediumTitle
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema

@Composable
fun ObservationList(observations: List<ObservationSchema>) {
    Column(
        verticalArrangement = Arrangement.Top
    ) {
        observations.forEach { observation ->
            Row(
                modifier = Modifier
                    .clickable {

                    }
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 8.dp)
                ) {
                    MediumTitle(text = observation.observationTitle)
                    BasicText(text = observation.observationType, color = MoreColors.Secondary)
                }
                Icon(
                    Icons.Default.ArrowForwardIos,
                    contentDescription = "View observation details",
                    tint = MoreColors.Primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            MoreDivider()
        }
    }
}