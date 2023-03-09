package io.redlink.more.more_app_mutliplatform.android.activities.setting

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.*
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreGray
import io.redlink.more.more_app_mutliplatform.android.ui.theme.moreImportant

class SettingsActivity: ComponentActivity() {

    private val viewModel = SettingsViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.createCoreViewModel(this)
        setContent {
            MoreBackground(rightCornerContent = {
                IconButton(onClick = { this.finish() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.more_close_overlay))
                }
            }) {
                SettingsView(model = viewModel)
            }
        }
    }
}


@Composable
fun SettingsView(
    model: SettingsViewModel
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f)
    ) {
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(vertical = 16.dp)) {

        item{
                Title(text = getStringResource(id = R.string.more_settings_title))

                Spacer(Modifier.height(18.dp))

                BasicText(
                    text = getStringResource(id = R.string.more_settings_permission_information),
                    color = MoreColors.TextColor,
                )

                Spacer(Modifier.height(18.dp))

                SmallTextButton(text = getStringResource(id = R.string.more_settings_refresh_study), enabled = true) {

                }

                Spacer(Modifier.height(24.dp))

                Accordion(
                    title = getStringResource(id = R.string.more_study_consent),
                    description = model.permissionModel.value.studyParticipantInfo,
                    hasCheck = true,
                    hasSmallTitle = true,
                    hasPreview = false
                )
            }

            items(model.permissionModel.value.consentInfo) { consentInfo ->
                Accordion(title = consentInfo.title,
                    description = consentInfo.info,
                    hasCheck = true,
                    hasSmallTitle = true,
                    hasPreview = false
                )
            }

            item{
                Spacer(Modifier.height(24.dp))

                SmallTextButton(
                    text = getStringResource(id = R.string.more_settings_exit_dialog_title),
                    buttonColors = ButtonDefaults.moreImportant(),
                    borderStroke = MoreColors.borderImportant()) {
                    model.removeParticipation(context)
                }

                SmallTextButton(
                    text = getStringResource(id = R.string.more_back),
                    buttonColors = ButtonDefaults.moreGray(),
                    borderStroke = MoreColors.borderGray(),
                    enabled = true
                ) {
                    (context as? Activity)?.finish()
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}