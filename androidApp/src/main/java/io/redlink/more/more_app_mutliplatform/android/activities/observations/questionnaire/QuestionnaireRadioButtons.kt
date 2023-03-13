package io.redlink.more.more_app_mutliplatform.android.activities.observations.questionnaire

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun QuestionnaireRadioButtons(model: QuestionnaireViewModel) {
    val selectedValue = remember { mutableStateOf("") }

    val isSelectedItem: (String) -> Boolean = { selectedValue.value == it }
    val onChangeState: (String) -> Unit = {
        selectedValue.value = it
        model.setAnswer()
    }

    val items = remember {
        model.answers
    }

    LazyColumn {
        items(items) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = isSelectedItem(item),
                        onClick = { onChangeState(item) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = isSelectedItem(item),
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MoreColors.Primary,
                        unselectedColor = MoreColors.Primary,
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