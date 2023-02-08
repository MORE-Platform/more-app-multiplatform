package io.redlink.more.more_app_mutliplatform.android.shared_composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.main.composables.NotificationButton
import io.redlink.more.more_app_mutliplatform.android.activities.main.composables.SettingsButton
import io.redlink.more.more_app_mutliplatform.android.extensions.Image
import io.redlink.more.more_app_mutliplatform.android.extensions.color
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MorePlatformTheme

@Composable
fun MoreBackground(
    rightCornerContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    MorePlatformTheme() {
        Surface(
            modifier = Modifier
                .fillMaxSize(1f),
            color = color(id = R.color.more_main_background)
        ) {
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize(1f)
                    .padding(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                ) {
                    Image(
                        id = R.drawable.more_logo_blue_vector,
                        contentDescription = "More Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .aspectRatio(1.5f)
                    )
                    Box(modifier = Modifier.weight(0.1f))
                    if (rightCornerContent != null) {
                        Box(modifier = Modifier.width(IntrinsicSize.Min),
                            contentAlignment = Alignment.CenterEnd) {
                            rightCornerContent()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MoreLoginPreview() {
    MoreBackground(rightCornerContent = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NotificationButton(
                onClick = { },
            )
            SettingsButton(
                onClick = { },
            )
        }
    }) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier
                .fillMaxWidth(0.95f)
            ) {
                item {
                    Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MoreColors.Divider)
                    TableItem()
                    TableItem()
                }
            }
        }

    }
}

@Composable
fun TableItem() {
    val activityEnabled = false
    val hasActivity = true
    val leadingIndex = 2
    val infoOrError = 1
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.Top,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(if (leadingIndex != 0) 0.1f else 0.05f)
                    .fillMaxWidth()
            ) {
                when (leadingIndex) {
                    1 -> {
                        Text(
                            text = "1.",
                            fontWeight = FontWeight.Medium,
                            color = MoreColors.MainTitle
                        )
                    }
                    2 -> {
                        when(infoOrError) {
                            0 -> Icon(Icons.Default.Info, contentDescription = "Information notification", tint = MoreColors.Main)
                            1 -> Icon(Icons.Default.PriorityHigh, contentDescription = "Important notification", tint = MoreColors.Important)
                            else -> {}
                        }
                    }
                    else -> {}
                }
            }
            Column(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 4.dp)
            ) {
                Text(
                    text = "Bewegung am Abend",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MoreColors.MainTitle,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Bewegungssensor",
                    color = MoreColors.Main,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Activity is to to Time:",
                            tint = MoreColors.Main
                        )
                        Text(
                            text = "17:00",
                            color = MoreColors.Main,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Timelapse,
                            contentDescription = "Activity runs for:",
                            tint = MoreColors.Main
                        )
                        Text(
                            text = "2 Stunden",
                            color = MoreColors.Main,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (hasActivity) {
                    TextButton(
                        onClick = { },
                        colors = ButtonDefaults
                            .textButtonColors(
                                contentColor = MoreColors.White,
                                backgroundColor = MoreColors.Main,
                                disabledContentColor = MoreColors.InactiveText),
                        enabled = activityEnabled,
                        modifier = Modifier
                            .weight(0.1f)
                            .fillMaxWidth(1f)
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "Bewegung aufzeichnen")
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ArrowForwardIos,
                    contentDescription = "See Details",
                    tint = MoreColors.Main,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                if (hasActivity) {
                    Box(modifier = Modifier.weight(0.75f))
                }
            }
        }

    }
    Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MoreColors.Divider)
}

