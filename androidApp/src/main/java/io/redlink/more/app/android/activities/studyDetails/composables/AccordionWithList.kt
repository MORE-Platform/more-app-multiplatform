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
package io.redlink.more.app.android.activities.studyDetails.composables

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
import androidx.navigation.NavController
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.MediumTitle
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import org.mongodb.kbson.ObjectId

@Composable
fun AccordionWithList(navController: NavController, title: String, observations: List<ObservationSchema>) {
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .clickable {
                    open.value = !open.value
                }
        ) {
            MediumTitle(
                text = title
            )
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
            ObservationList(observations = observations, navController = navController)
        }


    }
}