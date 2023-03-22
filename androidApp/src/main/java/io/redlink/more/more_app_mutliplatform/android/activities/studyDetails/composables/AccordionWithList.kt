package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import io.realm.kotlin.types.RealmList
import io.redlink.more.more_app_mutliplatform.android.shared_composables.Heading
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema

@Composable
fun AccordionWithList(title: String, observations: RealmList<ObservationSchema>) {
    val open = remember {
        mutableStateOf(false)
    }

    val angle: Float by animateFloatAsState(
        targetValue = if (open.value) 180f else 0f,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearEasing
        )
    )
    Column(
        modifier = Modifier
            .padding(start = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    open.value = !open.value
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            ) {
                Heading(text = title)
                Icon(
                    Icons.Rounded.ExpandMore,
                    tint = MoreColors.Primary,
                    contentDescription = "View observation modules of the study",
                    modifier = Modifier.rotate(angle)
                )
            }
            MoreDivider()
            Spacer(Modifier.height(12.dp))
            if (open.value) {
                ObservationList(observations = observations)
            }
        }
    }


}