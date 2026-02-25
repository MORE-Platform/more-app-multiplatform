package io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.questionType

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
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
import io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.QuestionViewModel
import io.redlink.umm.blendedcare.app.android.theme.MoreColors

@Composable
fun QuestionnaireCheckboxes(
    model: QuestionViewModel,
    selectedAnswer: Any?,
    onAnswerSelected: (Any) -> Unit
) {
    val observation by model.coreViewModel.questionModel.collectAsStateWithLifecycle(null)
    val answers = (observation?.answers ?: mutableSetOf()).toList()

    val selected: Set<String> = (selectedAnswer as? List<*>)
        ?.filterIsInstance<String>()
        ?.toSet()
        ?: emptySet()

    LazyColumn {
        items(answers.size) { idx ->
            val item = answers[idx]
            val checked = selected.contains(item)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .toggleable(
                        value = checked,
                        onValueChange = { isChecked ->
                            val newSelection = if (isChecked) selected + item else selected - item

                            val ordered = answers.filter { it in newSelection }

                            onAnswerSelected(ordered)
                        },
                        role = Role.Checkbox,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MoreColors.Primary,
                        uncheckedColor = MoreColors.Primary,
                        disabledColor = MoreColors.SecondaryMedium
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