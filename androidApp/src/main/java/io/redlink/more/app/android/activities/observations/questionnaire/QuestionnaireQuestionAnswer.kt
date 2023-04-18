package io.redlink.more.more_app_mutliplatform.android.activities.observations.questionnaire

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun QuestionnaireQuestionAnswer(model: QuestionnaireViewModel) {
    Spacer(Modifier.height(16.dp))

    Text(
        text = model.question.value,
        maxLines = 5,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = MoreColors.Primary
    )

    Spacer(Modifier.height(12.dp))

    QuestionnaireRadioButtons(model = model)

    Spacer(Modifier.height(4.dp))
}