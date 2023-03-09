package io.redlink.more.more_app_mutliplatform.android.activities.observations.questionnaire

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreDefault

@Composable
fun QuestionnaireButtons(model: QuestionnaireViewModel) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f)
            .padding(bottom = 20.dp)

    ) {
        Button(
            onClick = {
                if (model.answerSet.value) {
                    //model.uploadChosenAnswer()
                    model.goToNextActivity(context)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.more_questionnaire_select),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            },
            colors = ButtonDefaults.moreDefault(),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(6.dp)
        ) {
            Text(text = getString(R.string.more_quest_complete))
        }

        Button(
            onClick = {
                model.returnToMain(context)
                      },
            colors = ButtonDefaults
                .buttonColors(
                    backgroundColor = MoreColors.MainCancel,
                    contentColor = MoreColors.White
                ),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(6.dp)
        ) {
            Text(text = getString(R.string.more_quest_abort))
        }
    }
}