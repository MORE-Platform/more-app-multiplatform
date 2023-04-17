package io.redlink.more.app.android.activities.setting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.Accordion
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreImportant
import io.redlink.more.app.android.R



@Composable
fun SettingsView(
    model: SettingsViewModel,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {

            item {
                BasicText(
                    text = getStringResource(id = R.string.more_settings_permission_information),
                    color = MoreColors.TextDefault,
                )

                Spacer(Modifier.height(18.dp))

                SmallTextButton(
                    text = getStringResource(id = R.string.more_settings_refresh_study),
                    enabled = true
                ) {

                }

                model.permissionModel.value?.let {
                    Spacer(Modifier.height(24.dp))

                    Accordion(
                        title = getStringResource(id = R.string.more_study_consent),
                        description = it.studyParticipantInfo,
                        hasCheck = true,
                        hasSmallTitle = true,
                        hasPreview = false
                    )
                }

            }

            items(model.permissionModel.value?.consentInfo ?: emptyList()) { consentInfo ->
                Accordion(
                    title = consentInfo.title,
                    description = consentInfo.info,
                    hasCheck = true,
                    hasSmallTitle = true,
                    hasPreview = false
                )
            }

            item {
                Spacer(Modifier.height(24.dp))

                SmallTextButton(
                    text = getStringResource(id = R.string.more_settings_exit_dialog_title),
                    buttonColors = ButtonDefaults.moreImportant(),
                    borderStroke = MoreColors.borderImportant()
                ) {
                    model.openLeaveStudyLvlOne(context)
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}