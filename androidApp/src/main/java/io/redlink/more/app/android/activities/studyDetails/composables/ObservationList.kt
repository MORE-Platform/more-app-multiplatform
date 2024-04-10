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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.MediumTitle
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema

@Composable
fun ObservationList(navController: NavController, observations: List<ObservationSchema>) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        observations.forEach { observation ->
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable {
                        navController.navigate(
                            NavigationScreen.OBSERVATION_DETAILS.navigationRoute(
                                "observationId" to observation.observationId
                            )
                        )
                    }
                    .padding(top = 12.dp, bottom = 0.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    MediumTitle(text = observation.observationTitle)
                    BasicText(text = observation.observationType, color = MoreColors.Secondary)
                }
                Icon(
                    Icons.Default.ArrowForwardIos,
                    contentDescription = "View observation details",
                    tint = MoreColors.Primary,
                    modifier = Modifier
                        .size(16.dp)
                )
            }
            MoreDivider()
        }
    }
}