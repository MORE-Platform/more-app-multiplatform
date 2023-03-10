package io.redlink.more.more_app_mutliplatform.android.activities.setting.leave_study

import android.app.Activity
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardActivity
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsActivity
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.Image
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivityAndClearStack
import io.redlink.more.more_app_mutliplatform.android.shared_composables.*
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreApproved
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreImportant

class LeaveStudyLevelTwoActivity: ComponentActivity() {
    private val viewModel = SettingsViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.createCoreViewModel(this)
        setContent {
            MoreBackground(rightCornerContent = {
                IconButton(
                    onClick = {
                        showNewActivityAndClearStack(this, DashboardActivity::class.java)
                        showNewActivity(this, SettingsActivity::class.java)
                    }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.more_close_overlay)
                    )
                }
            }) {
                LeaveStudyLvlOneTwo(model = viewModel)
            }
        }
    }
}

@Composable
fun LeaveStudyLvlOneTwo(model: SettingsViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f)
    ) {

        Title(text = model.permissionModel.value.studyTitle)

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
            modifier = Modifier
                .fillMaxWidth()
        )


        Spacer(Modifier.height(10.dp))

        SmallTitle(
            text = stringResource(id = R.string.more_settings_withdraw_question_confirm),
            color = MoreColors.TextDefault,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        SmallTextButton(
            text = stringResource(id = R.string.more_settings_continue),
            buttonColors = ButtonDefaults.moreApproved(),
            borderStroke = MoreColors.borderApproved()
        ) {
            showNewActivityAndClearStack(context, DashboardActivity::class.java)
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
                model.removeParticipation(context)
            }
        }
    }
}