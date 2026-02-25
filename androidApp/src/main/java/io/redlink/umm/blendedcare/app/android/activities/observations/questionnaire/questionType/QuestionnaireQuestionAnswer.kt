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
package io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.questionType

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.QuestionViewModel
import io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.QuestionnaireRadioButtons
import io.redlink.umm.blendedcare.app.android.theme.MoreColors
import io.redlink.umm.participant.models.QuestionType

@Composable
fun QuestionnaireQuestionAnswer(
    model: QuestionViewModel,
    selectedAnswer: Any?,
    onAnswerSelected: (Any) -> Unit
) {
    val observation by model.coreViewModel.questionModel.collectAsStateWithLifecycle(null)
    val question = observation?.question ?: ""
    val type = observation?.type ?: QuestionType.NON

    Spacer(Modifier.height(16.dp))

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = question,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MoreColors.Primary
        )
    }

    Spacer(Modifier.height(12.dp))

    when (type) {
        QuestionType.SINGLE_CHOICE -> QuestionnaireRadioButtons(
            model = model,
            selectedAnswer = selectedAnswer,
            onAnswerSelected = onAnswerSelected
        )

        QuestionType.MULTIPLE_CHOICE -> QuestionnaireCheckboxes(
            model,
            selectedAnswer = selectedAnswer,
            onAnswerSelected = onAnswerSelected
        )

        else -> {
            // Keep dynamic: each new subview should call onAnswerSelected(...) with the correct type.
        }
    }

    Spacer(Modifier.height(4.dp))
}