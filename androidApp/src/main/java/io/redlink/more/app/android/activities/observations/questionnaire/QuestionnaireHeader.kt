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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.shared_composables.HeaderDescription
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.ui.theme.MoreColors


@Composable
fun QuestionnaireHeader(model: QuestionnaireViewModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(2.dp))
    {
        HeaderTitle(title = model.observationTitle.value)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .heightIn(max = 100.dp)
                .fillMaxWidth()
        ) {
            item {
                Text(
                    text = stringResource(R.string.more_questionnaire_type),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MoreColors.Primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            item {
                HeaderDescription(description = model.observationParticipantInfo.value)
            }
        }

        Spacer(Modifier.height(8.dp))

        Divider(Modifier.height(1.dp))
    }
}