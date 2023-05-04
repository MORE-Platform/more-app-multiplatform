package io.redlink.more.app.android.activities.setting.leave_study

import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.*
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreApproved
import io.redlink.more.app.android.ui.theme.moreImportant
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.leaveStudy.LeaveStudyViewModel

@Composable
fun LeaveStudyView(navController: NavController, viewModel: LeaveStudyViewModel) {
    val context = LocalContext.current

    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.BLUETOOTH_CONNECTION.route)
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalArrangement = Arrangement.Center
        ) {
            viewModel.permissionModel.value?.let {
                Title(text = it.studyTitle, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(80.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    id = R.drawable.warning_exclamation,
                    contentDescription = "More Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .aspectRatio(1.5f)
                )
            }

            Spacer(Modifier.height(18.dp))

            SmallTitle(
                text = stringResource(id = R.string.more_settings_withdraw_statement),
                color = MoreColors.Important,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(80.dp))

            SmallTitle(
                text = stringResource(id = R.string.more_settings_withdraw_question),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            SmallTextButton(
                text = stringResource(id = R.string.more_settings_continue),
                buttonColors = ButtonDefaults.moreApproved(),
                borderStroke = MoreColors.borderApproved()
            ) {
                (context as? Activity)?.onBackPressed()
            }

            SmallTextButton(
                text = getStringResource(id = R.string.more_settings_withdraw_from_study),
                buttonColors = ButtonDefaults.moreImportant(),
                borderStroke = MoreColors.borderImportant()
            ) {
                navController.navigate(NavigationScreen.LEAVE_STUDY_CONFIRM.route)
            }
        }
    }
}