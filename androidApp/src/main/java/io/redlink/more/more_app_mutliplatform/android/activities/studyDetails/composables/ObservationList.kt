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
import io.realm.kotlin.types.RealmList
import io.redlink.more.more_app_mutliplatform.android.shared_composables.BasicText
import io.redlink.more.more_app_mutliplatform.android.shared_composables.Heading
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema

@Composable
fun ObservationList(observations: RealmList<ObservationSchema>) {
    Column(
        verticalArrangement = Arrangement.Top
    ) {
        observations.forEach { observation ->
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clickable {

                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 8.dp)
                ) {
                    Heading(
                        text = observation.observationTitle,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BasicText(text = observation.observationType, color = MoreColors.Secondary)
                }
                Icon(
                    Icons.Default.ArrowForwardIos,
                    contentDescription = "View observation details",
                    tint = MoreColors.Secondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            MoreDivider()
        }
    }
}