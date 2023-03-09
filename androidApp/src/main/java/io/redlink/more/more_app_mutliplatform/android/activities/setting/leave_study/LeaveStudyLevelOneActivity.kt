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
import io.redlink.more.more_app_mutliplatform.android.activities.setting.SettingsViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.Image
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivityAndClearStack
import io.redlink.more.more_app_mutliplatform.android.shared_composables.*
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreDone
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreImportant

class LeaveStudyLevelOneActivity: ComponentActivity() {
    private val viewModel = SettingsViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.createCoreViewModel(this)
        setContent {
            MoreBackground(rightCornerContent = {
                IconButton(onClick = { this.finish() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.more_close_overlay)
                    )
                }
            }) {
                LeaveStudyLvlOneView(model = viewModel)
            }
        }
    }
}

@Composable
fun LeaveStudyLvlOneView(model: SettingsViewModel) {
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

        Title(
            text = stringResource(id = R.string.more_settings_withdraw_statement),
            color = MoreColors.Important,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )


        Spacer(Modifier.height(80.dp))

        SmallTitle(text = stringResource(id = R.string.more_settings_withdraw_question))

        Spacer(Modifier.height(18.dp))

        SmallTextButton(
            text = stringResource(id = R.string.more_settings_continue),
            buttonColors = ButtonDefaults.moreDone(),
            borderStroke = MoreColors.borderDone()
        ) {
            showNewActivityAndClearStack(context, DashboardActivity::class.java)
        }

        SmallTextButton(
            text = getStringResource(id = R.string.more_settings_withdraw_from_study),
            buttonColors = ButtonDefaults.moreImportant(),
            borderStroke = MoreColors.borderImportant()
        ) {
            model.openLeaveStudyLvlTwo(context)
        }
    }
}