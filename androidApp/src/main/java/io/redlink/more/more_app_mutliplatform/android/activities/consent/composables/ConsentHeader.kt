package io.redlink.more.more_app_mutliplatform.android.activities.consent.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun ConsentHeader(studyTitle: String, studyParticipantInfo: String ,model: ConsentViewModel) {
    var participantInfoOverflow by remember {mutableStateOf(false)}
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

    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Text(
            text = studyTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoreColors.Primary,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(8.dp))

        Text(
            text = studyParticipantInfo,
            color = MoreColors.Secondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            maxLines = if (open.value) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = {
                textLayoutResult ->
                if(textLayoutResult.hasVisualOverflow)
                    participantInfoOverflow = true
            }
        )

        if(participantInfoOverflow){
            Spacer(Modifier.padding(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()){
                Text(
                    text = if (open.value) "Read Less" else "Read More",
                    color = MoreColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = { open.value = !open.value},
                ) {
                    Icon(
                        Icons.Rounded.ExpandMore,
                        contentDescription = getStringResource(id = R.string.more_endpoint_rotatable_arrow_description),
                        tint = MoreColors.Primary,
                        modifier = Modifier.rotate(angle)
                    )
                }
            }
        }
    }
}