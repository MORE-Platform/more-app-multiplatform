package io.redlink.umm.blendedcare.app.android.activities.subcomponents

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.activities.ContentActivity
import io.redlink.umm.blendedcare.app.android.extensions.getStringResource
import io.redlink.umm.blendedcare.app.android.extensions.showNewActivityAndClearStack
import io.redlink.umm.blendedcare.app.android.shared_composables.SmallTextButton
import io.redlink.umm.blendedcare.app.android.theme.MoreColors
import io.redlink.umm.blendedcare.app.android.theme.moreImportant
import io.redlink.umm.participant.AlertController
import io.redlink.umm.participant.models.AlertDialogModel

@Composable
fun ExitButton(
) {
    val context = LocalContext.current
    SmallTextButton(
        text = getStringResource(id = R.string.more_settings_resign_confirm),
        buttonColors = ButtonDefaults.moreImportant(),
        borderStroke = MoreColors.borderImportant(),
        modifier = Modifier
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
                    BlendedCareApplication.shared!!.exitStudy {
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