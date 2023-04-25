package io.redlink.more.app.android.activities.setting.leave_study

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.*
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreApproved
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.leaveStudy.LeaveStudyViewModel

@Composable
fun LeaveStudyConfirmView(navController: NavController, viewModel: LeaveStudyViewModel) {
    val context = LocalContext.current

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

            BasicText(
                text = stringResource(id = R.string.more_settings_withdraw_statement_long),
                color = MoreColors.TextDefault,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )


            Spacer(Modifier.height(10.dp))

            SmallTitle(
                text = stringResource(id = R.string.more_settings_withdraw_question_confirm),
                color = MoreColors.Primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(32.dp))

            SmallTextButton(
                text = stringResource(id = R.string.more_settings_continue),
                buttonColors = ButtonDefaults.moreApproved(),
                borderStroke = MoreColors.borderApproved()
            ) {
                navController.navigate(NavigationScreen.INFO.route)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                SwipeButton(
                    text = getStringResource(id = R.string.more_settings_resign_swipe),
                    isComplete = false
                ) {
                    viewModel.removeParticipation(context)
                }
            }
        }
    }
}