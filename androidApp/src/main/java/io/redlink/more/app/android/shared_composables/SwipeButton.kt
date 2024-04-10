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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowRightAlt
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.ui.theme.MoreColors
import kotlin.math.roundToInt

@Composable
fun SwipeIndicator(
    modifier: Modifier = Modifier,
    color: Color = MoreColors.Important,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .aspectRatio(
                ratio = 1.0F,
                matchHeightConstraintsFirst = true,
            )
            .background(color)
    ) {
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeButton(
    text: String,
    isComplete: Boolean,
    doneImageVector: ImageVector = Icons.Rounded.Done,
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke = MoreColors.borderImportant(),
    backgroundColor: Color = MoreColors.ImportantMedium,
    swipeBtnColor: Color = MoreColors.Important,
    textColor: Color = MoreColors.Important,
    iconColor: Color = MoreColors.White,
    onSwipeComplete: () -> Unit,
) {
    val width = 220.dp
    val widthInPx = with(LocalDensity.current) {
        width.toPx()
    }
    val anchors = mapOf(
        0F to 0,
        widthInPx to 1,
    )
    val swipeableState = rememberSwipeableState(0)
    val (swipeComplete, setSwipeComplete) = remember {
        mutableStateOf(false)
    }
    val alpha: Float by animateFloatAsState(
        targetValue = if (swipeComplete) {
            0F
        } else {
            1F
        },
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing,
        )
    )

    LaunchedEffect(
        key1 = swipeableState.currentValue,
    ) {
        if (swipeableState.currentValue == 1) {
            setSwipeComplete(true)
            onSwipeComplete()
        }
    }

    Box(
        contentAlignment = Alignment.Center,

        modifier = modifier
            .padding(
                vertical = 12.dp,
            )
            .clip(MaterialTheme.shapes.small)
            .animateContentSize()
            .border(borderStroke)
            .background(backgroundColor)
            .then(
                if (swipeComplete) {
                    Modifier
                        .width(64.dp)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .requiredHeight(48.dp),
    ) {
        SwipeIndicator(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .alpha(alpha)
                .offset {
                    IntOffset(swipeableState.offset.value.roundToInt(), 0)
                }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ ->
                        FractionalThreshold(0.9F)
                    },
                    orientation = Orientation.Horizontal,
                ),
            color = swipeBtnColor,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(swipeableState.offset.value.roundToInt(), 0)
                },
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alpha)
                    .padding(start = 38.dp)
            )

            Icon(
                imageVector = Icons.Rounded.ArrowRightAlt,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier
                    .size(36.dp)
            )
        }


        AnimatedVisibility(
            visible = swipeComplete && !isComplete,
        ) {
            CircularProgressIndicator(
                color = iconColor,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .padding(4.dp),
            )
        }
        AnimatedVisibility(
            visible = isComplete,
            enter = fadeIn(),
            exit = fadeOut(),

            ) {
            Icon(
                imageVector = doneImageVector,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp),
            )
        }
    }
}