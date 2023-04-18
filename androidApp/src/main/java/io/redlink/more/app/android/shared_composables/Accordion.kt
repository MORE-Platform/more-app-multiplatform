package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTitle
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.R


@Composable
fun Accordion(
    title: String,
    description: String,
    hasCheck: Boolean = false,
    hasSmallTitle: Boolean = false,
    hasPreview: Boolean = true
) {
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

    Row(verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp)) {


            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        open.value = !open.value
                    }
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()

                ) {

                    if(hasCheck) {
                        IconInline(
                            icon = Icons.Rounded.Done,
                            color = MoreColors.Approved,
                            contentDescription = getStringResource(id = R.string.more_approved)
                        )
                    }

                    if(hasSmallTitle) {
                        SmallTitle(text = title, modifier = Modifier.weight(0.9f))
                    } else {
                        MediumTitle(text = title, modifier = Modifier.weight(0.9f))
                    }
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = getStringResource(id = R.string.more_endpoint_rotatable_arrow_description),
                            tint = MoreColors.Primary,
                            modifier = Modifier.rotate(angle)
                        )

                }

                Divider(
                    color = MoreColors.Primary,
                    modifier = Modifier.padding(4.dp)
                )
            }

            if(hasPreview) {
                Text(
                    text = description,
                    color = if (open.value) MoreColors.Primary else MoreColors.TextInactive,
                    maxLines = if(open.value) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = if (open.value) TextUnit.Unspecified else 14.sp
                )
                Spacer(Modifier.height(12.dp))
            } else if(open.value) {
                Text(
                    text = description,
                    color = MoreColors.Primary,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = TextUnit.Unspecified
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}