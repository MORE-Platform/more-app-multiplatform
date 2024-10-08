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
package io.redlink.more.app.android.activities.observations.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.shared_composables.HeaderDescription
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.ui.theme.morePrimary


@Composable
fun QuestionnaireResponseView(navController: NavController) {
    val title = stringResource(R.string.more_quest_thank_you)

    MoreBackground {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.8f)
        ) {
            Column {
                HeaderTitle(title = title)

                Spacer(Modifier.height(24.dp))

                HeaderDescription(description = stringResource(R.string.more_quest_validation))

                Spacer(Modifier.height(12.dp))

                HeaderDescription(description = stringResource(R.string.more_quest_thank_you_full))
            }
            TextButton(
                onClick = { navController.navigate(NavigationScreen.DASHBOARD.routeWithParameters()) },
                colors = ButtonDefaults.morePrimary(),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(
                    text = getStringResource(id = R.string.more_quest_return_button_title),
                )
            }
        }
    }
}