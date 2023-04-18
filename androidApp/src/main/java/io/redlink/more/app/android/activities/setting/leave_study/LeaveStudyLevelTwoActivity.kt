package io.redlink.more.app.android.activities.setting.leave_study

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
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.*
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreApproved
import io.redlink.more.app.android.R


class LeaveStudyLevelTwoActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = SettingsViewModel(this)
        setContent {
            MoreBackground(rightCornerContent = {
                IconButton(
                    onClick = {
                        finish()
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
        model.permissionModel.value?.let {
            Title(text = it.studyTitle)
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
            (context as? Activity)?.finish()
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