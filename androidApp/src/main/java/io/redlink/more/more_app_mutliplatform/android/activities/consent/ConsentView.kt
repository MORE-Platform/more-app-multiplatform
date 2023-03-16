package io.redlink.more.more_app_mutliplatform.android.activities.consent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.consent.composables.ConsentButtons
import io.redlink.more.more_app_mutliplatform.android.activities.consent.composables.ConsentHeader
import io.redlink.more.more_app_mutliplatform.android.activities.consent.composables.ConsentListItem
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MessageAlertDialog


@Composable
fun ConsentView(model: ConsentViewModel) {
    val context = LocalContext.current
    model.error.value?.let {
        MessageAlertDialog(title = getStringResource(id = R.string.more_permission_message_dialog_title),
            message = "$it\n ${getStringResource(id = R.string.more_permission_message_dialog_message_retry)}",
            positiveButtonTitle = getStringResource(id = R.string.more_permission_message_dialog_message_retry_button),
            onPositive = {
                model.acceptConsent(context)
                model.error.value = null
            },
            negativeButtonTitle = getStringResource(id = R.string.more_permission_message_dialog_message_cancel_button),
            onNegative = {
                model.decline()
                model.error.value = null
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.9f)
    ) {
        ConsentHeader(studyTitle = model.permissionModel.value.studyTitle, model.permissionModel.value.studyParticipantInfo, model)
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(vertical = 16.dp)) {

            items(model.permissionModel.value.consentInfo) { consentInfo ->
                ConsentListItem(title = consentInfo.title, description = consentInfo.info,
                    openInit = (consentInfo.title == "Study Consent"))
            }
        }

        Box(Modifier.weight(0.35f)) {
            ConsentButtons(model = model)
        }
    }
}