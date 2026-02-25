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
package io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.redlink.umm.blendedcare.app.android.theme.MoreColors

@Composable
fun QuestionnaireRadioButtons(
    model: QuestionViewModel,
    selectedAnswer: Any?,
    onAnswerSelected: (Any) -> Unit
) {
    val observation by model.coreViewModel.questionModel.collectAsStateWithLifecycle(null)
    val answers = (observation?.answers ?: mutableSetOf()).toList()

    LazyColumn {
        items(answers.size) { idx ->
            val item = answers[idx]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = (selectedAnswer as? String) == item,
                        onClick = { onAnswerSelected(item) },
                        role = Role.RadioButton,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = (selectedAnswer as? String) == item,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MoreColors.Companion.Primary,
                        unselectedColor = MoreColors.Companion.Primary,
                        disabledColor = MoreColors.Companion.SecondaryMedium
                    ),
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = item,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                )
            }
        }
    }
}