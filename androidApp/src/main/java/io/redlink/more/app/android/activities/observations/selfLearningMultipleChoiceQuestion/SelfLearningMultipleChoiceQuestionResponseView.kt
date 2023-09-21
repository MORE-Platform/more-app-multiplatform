package io.redlink.more.app.android.activities.observations.selfLearningMultipleChoiceQuestion

import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.HeaderDescription
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.ui.theme.morePrimary
import io.redlink.more.app.android.R


@Composable
fun SelfLearningMultipleChoiceQuestionResponseView(navController: NavController) {
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
                HeaderTitle(title = title)

                Spacer(Modifier.height(24.dp))

                HeaderDescription(description = getString(R.string.more_quest_validation))

                Spacer(Modifier.height(12.dp))

                HeaderDescription(description = getString(R.string.more_quest_thank_you_full))
            }
            TextButton(
                onClick = { navController.navigate(NavigationScreen.DASHBOARD.route) },
                colors = ButtonDefaults.morePrimary(),
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