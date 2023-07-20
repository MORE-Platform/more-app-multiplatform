package io.redlink.more.app.android.activities.observations.questionnaire

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.observations.pushButton.PushButtonViewModel
import io.redlink.more.app.android.extensions.getString
import io.redlink.more.app.android.ui.theme.morePrimary
import io.redlink.more.app.android.ui.theme.moreSecondary

@Composable
fun PushButtonView(navController: NavController, viewModel: PushButtonViewModel) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.PUSH_BUTTON.route)
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .fillMaxWidth(0.8f)
    ) {
        Button(
            onClick = {viewModel.click()},
            colors = ButtonDefaults.morePrimary(),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(6.dp)
        ) {
            Text(text = viewModel.buttonText.value)
        }
        Button(
            onClick = {
                viewModel.finish()
                navController.navigate(NavigationScreen.DASHBOARD.route) },
            colors = ButtonDefaults.moreSecondary(),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(6.dp)
        ) {
            Text(text = getString(R.string.more_close))
        }
    }
}