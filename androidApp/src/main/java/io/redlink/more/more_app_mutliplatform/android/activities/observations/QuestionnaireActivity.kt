package io.redlink.more.more_app_mutliplatform.android.activities.observations

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

class QuestionnaireActivity: ComponentActivity() {
    private val questionnaireViewModel = QuestionnaireViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestionnaireView(questionnaireViewModel)
        }
    }
}

@Composable
fun QuestionnaireView(model: QuestionnaireViewModel){
    MoreBackground(rightCornerContent = {
        Icon(Icons.Default.Clear, contentDescription = "Close Observation")
    }) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth(0.8f)
        ) {
            QuestionPart(model)
            QuestionAnswer(model = model)
        }
        QuestionButtons(model = model)
    }
}

@Composable
fun QuestionPart(model: QuestionnaireViewModel) {
    Text(
        text = model.observationTitle.value,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = MoreColors.MainTitle
    )

    Spacer(Modifier.height(12.dp))

    LazyColumn(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .heightIn(max = 100.dp)
            .fillMaxWidth()
    ) {
        item{
            Text(
                text = "Simple Questionnaire",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MoreColors.Main,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        item {
            Text(
                text = model.observationParticipantInfo.value,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MoreColors.Main
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    Divider(Modifier.height(1.dp))
}

@Composable
fun QuestionAnswer(model: QuestionnaireViewModel) {
    Spacer(Modifier.height(16.dp))

    Text(
        text = model.question.value,
        maxLines = 5,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = MoreColors.Main
    )

    Spacer(Modifier.height(12.dp))

    MultipleRadioButtons(model = model)

    Spacer(Modifier.height(4.dp))
}

@Composable
fun MultipleRadioButtons(model: QuestionnaireViewModel) {
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
                        selectedColor = MoreColors.Main,
                        unselectedColor = MoreColors.Main,
                        disabledColor = MoreColors.Inactivity
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

@Composable
fun QuestionButtons(model: QuestionnaireViewModel) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f)
            .padding(bottom = 36.dp)

    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    model.returnToMain(context)
                },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = MoreColors.Main,
                        contentColor = MoreColors.White
                    ),
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(text = getString(R.string.more_quest_abort))
            }
            Button(
                onClick = {
                    if (model.answerSet.value) {
                        //model.uploadChosenAnswer()
                        model.goToNextActivity(context)
                    } else {
                        Toast.makeText(context, "Please select an answer!", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = MoreColors.Main,
                        contentColor = MoreColors.White
                    ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 32.dp)
            ) {
                Text(text = getString(R.string.more_quest_complete))
            }

        }
    }
}