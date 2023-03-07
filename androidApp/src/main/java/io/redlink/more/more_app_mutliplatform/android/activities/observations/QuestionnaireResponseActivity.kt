package io.redlink.more.more_app_mutliplatform.android.activities.observations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

class QuestionnaireResponseActivity : ComponentActivity() {
    private val questionnaireViewModel = QuestionnaireViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestionnaireResponseView(model = questionnaireViewModel)
        }
    }
}

@Composable
fun QuestionnaireResponseView(model: QuestionnaireViewModel) {
    val context = LocalContext.current
    val title = getString(R.string.more_quest_thank_you)

    MoreBackground {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.8f)
        ) {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MoreColors.MainTitle,

                    )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = getString(R.string.more_quest_validation),
                    fontSize = 14.sp,
                    color = MoreColors.MainTitle
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    text = getString(R.string.more_quest_thank_you_full),
                    fontSize = 14.sp,
                    color = MoreColors.MainTitle
                )
            }
            TextButton(
                onClick = { model.closeActivity(context, true) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MoreColors.White,
                    backgroundColor = MoreColors.Main
                ),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(
                    text = getStringResource(id = R.string.more_quest_return_button_title),
                )
            }
        }
    }
}

//@Preview(showSystemUi = true)
//@Composable
//fun QuestRespPreview() {
//    QuestionnaireResponseView()
//}