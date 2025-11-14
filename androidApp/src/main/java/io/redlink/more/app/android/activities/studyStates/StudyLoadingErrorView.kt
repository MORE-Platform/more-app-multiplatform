package io.redlink.more.app.android.activities.studyStates

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.ContentActivity
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.showNewActivityAndClearStack
import io.redlink.more.app.android.shared_composables.MediumTitle
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.Title
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreImportant
import io.redlink.more.more_app_mutliplatform.AlertController
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel

@Composable
fun StudyLoadingErrorView() {
    val context = LocalContext.current
    MoreBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
            ) {
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
                Title(
                    text = getStringResource(id = R.string.study_loading_error_title),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MediumTitle(
                        getStringResource(R.string.study_loading_error_message),
                        textAlign = TextAlign.Center
                    )
                }
            }

            SmallTextButton(
                text = getStringResource(id = R.string.more_settings_resign_confirm),
                buttonColors = ButtonDefaults.moreImportant(),
                borderStroke = MoreColors.borderImportant(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                AlertController.openAlertDialog(
                    AlertDialogModel(
                        title = context.getString(R.string.more_settings_withdraw_question_confirm),
                        message = context.getString(R.string.more_settings_withdraw_statement_long),
                        confirmLabel = context.getString(R.string.more_settings_resign_confirm),
                        cancelLabel = context.getString(R.string.more_settings_continue),
                        onConfirm = {
                            WorkManager.getInstance(context).cancelAllWork()
                            MoreApplication.shared!!.exitStudy {
                                (context as? Activity)?.let { activity ->
                                    activity.finish()
                                    showNewActivityAndClearStack(
                                        activity,
                                        ContentActivity::class.java
                                    )
                                }
                            }
                        }
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun StudyLoadingErrorPreview() {
    StudyLoadingErrorView()
}