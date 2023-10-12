package io.redlink.more.app.android.activities.observations.questionnaire

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
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.ui.theme.morePrimary
import io.redlink.more.app.android.R


@Composable
fun QuestionnaireButtons(navController: NavController, model: QuestionnaireViewModel) {
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
                if (model.answerSet.value.isNotBlank()) {
                    model.finish()
                    navController.navigate(NavigationScreen.QUESTIONNAIRE_RESPONSE.routeWithParameters())
                } else {
                    Toast.makeText(
                        context,
                        stringResource(R.string.more_questionnaire_select),
                        Toast.LENGTH_SHORT
                    )
                        .show()
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