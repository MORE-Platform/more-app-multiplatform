package io.redlink.more.app.android.activities.subcomponents

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import io.redlink.io.more.app.android.R
import io.redlink.more.AlertController
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.ContentActivity
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.showNewActivityAndClearStack
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.app.android.theme.moreImportant
import io.redlink.more.models.AlertDialogModel

@Composable
fun ExitButton(
) {
    val context = LocalContext.current
    SmallTextButton(
        text = getStringResource(id = R.string.more_settings_resign_confirm),
        buttonColors = ButtonDefaults.moreImportant(),
        borderStroke = MoreColors.Companion.borderImportant(),
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
                    MoreApplication.Companion.shared!!.exitStudy {
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