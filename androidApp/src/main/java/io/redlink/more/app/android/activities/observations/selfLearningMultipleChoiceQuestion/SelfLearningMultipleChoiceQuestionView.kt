package io.redlink.more.app.android.activities.observations.selfLearningMultipleChoiceQuestion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireButtons
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireQuestionAnswer
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.ErrorMessage

@Composable
fun SelfLearningMultipleChoiceQuestionView(navController: NavController, viewModel: SelfLearningMultipleChoiceQuestionViewModel) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.SELF_LEARNING_MULTIPLE_CHOICE_QUESTION.route)
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
    if (viewModel.hasData.value) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            SelfLearningMultipleChoiceQuestionAnswer(model = viewModel)
        }
        SelfLearningMultipleChoiceQuestionButtons(navController = navController, model = viewModel)
    } else {
        ErrorMessage(message = "${getStringResource(id = R.string.data_not_found)}!")
    }
}