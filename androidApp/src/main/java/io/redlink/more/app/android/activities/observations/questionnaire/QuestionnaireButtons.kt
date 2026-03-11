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

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.theme.morePrimary
import io.redlink.more.models.QuestionType

@Composable
fun QuestionnaireButtons(
    questionType: QuestionType,
    selectedAnswer: Any?,
    onFinish: (Any) -> Unit
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp)
    ) {
        Button(
            onClick = {
                val isValid = when (questionType) {
                    QuestionType.SINGLE_CHOICE -> (selectedAnswer as? String)?.isNotBlank() == true
                    QuestionType.MULTIPLE_CHOICE -> (selectedAnswer as? List<*>)?.isNotEmpty() == true
                    else -> selectedAnswer != null
                }

                if (isValid) {
                    onFinish(selectedAnswer!!)
                } else {
                    Toast.makeText(
                        context,
                        stringResource(R.string.more_questionnaire_select),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            colors = ButtonDefaults.morePrimary(),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(6.dp)
        ) {
            Text(text = stringResource(R.string.more_quest_complete))
        }
    }
}