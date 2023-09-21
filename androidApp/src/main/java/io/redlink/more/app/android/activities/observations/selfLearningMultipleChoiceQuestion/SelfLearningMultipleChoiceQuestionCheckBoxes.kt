package io.redlink.more.app.android.activities.observations.selfLearningMultipleChoiceQuestion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun SelfLearningMultipleChoiceQuestionCheckBoxes(model: SelfLearningMultipleChoiceQuestionViewModel) {
    val selectedValues =
        remember { mutableStateListOf<String>() } // Use a list to track selected items
    val userTextInput = remember { mutableStateOf("") } // Store user input text

    val isSelectedItem: (String) -> Boolean =
        {
            selectedValues.contains(it)
        }
    val toggleSelection: (String) -> Unit = { item ->
        if (isSelectedItem(item)) {
            selectedValues.remove(item)
        } else {
            if (item.isNotBlank())
                selectedValues.add(item)
        }
        model.setAnswer(selectedValues.toList()) // Pass the list of selected items to the ViewModel
    }

    val items = remember {
        model.answers
    }

    LazyColumn {
        items(items) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { toggleSelection(item) } // Use clickable instead of selectable
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = isSelectedItem(item),
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
        // Add the text box row
        item{
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userTextInput.value,
                    onValueChange = { newText ->
                        userTextInput.value = newText
                        model.userTextAnswer.value = userTextInput.value
                    },
                    placeholder = { Text("Enter your answer here") },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MoreColors.Primary,
                        focusedLabelColor = MoreColors.Primary,
                        backgroundColor = Color.Transparent,
                        unfocusedLabelColor = MoreColors.Primary,
                        errorLabelColor = MoreColors.Important,
                        cursorColor = MoreColors.Primary,
                        placeholderColor = MoreColors.TextInactive,
                        unfocusedBorderColor = MoreColors.Primary,
                        focusedBorderColor = MoreColors.Primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                )
            }
        }
    }
}