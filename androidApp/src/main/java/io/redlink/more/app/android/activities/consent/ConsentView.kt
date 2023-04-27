package io.redlink.more.app.android.activities.consent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.consent.composables.ConsentButtons
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.Accordion
import io.redlink.more.app.android.shared_composables.AccordionReadMore
import io.redlink.more.app.android.shared_composables.MessageAlertDialog
import io.redlink.more.app.android.ui.theme.MoreColors


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

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.9f)
    ) {
        item {
            Text(
                text = model.permissionModel.value.studyTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MoreColors.Primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.padding(8.dp))
            AccordionReadMore(
                title = "Participant Information",
                description = model.permissionModel.value.studyParticipantInfo,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        items(model.permissionModel.value.consentInfo) { consentInfo ->
            Accordion(
                title = consentInfo.title, description = consentInfo.info,
                hasCheck = true, hasPreview = (consentInfo.title == "Study Consent")
            )
        }
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }

        item {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            ) {
                ConsentButtons(model = model)
            }
        }
    }
}