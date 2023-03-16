package io.redlink.more.more_app_mutliplatform.android.activities.consent.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun ConsentListItem(title: String, description: String? = null, openInit: Boolean) {
    val open = remember {
        mutableStateOf(openInit)
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
            .weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = getStringResource(
                        id = R.string.more_permission_check_icon_description),
                    tint = MoreColors.Approved,
                )
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MoreColors.Primary,
                    modifier = Modifier
                        .weight(0.9f)
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
            Divider(
                color = MoreColors.Primary,
                modifier = Modifier.padding(bottom = 12.dp))
            Text(
                text = if (open.value) description
                    ?: "Do you authorize MORE to access your protected resources? Click the resources for which you want to grant access:"
                    else "",
                color = MoreColors.Secondary,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                fontSize = TextUnit.Unspecified
            )
        }
    }
}