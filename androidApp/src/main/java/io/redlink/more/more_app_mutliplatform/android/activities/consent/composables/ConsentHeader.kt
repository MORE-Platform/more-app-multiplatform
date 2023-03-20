package io.redlink.more.more_app_mutliplatform.android.activities.consent.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun ConsentHeader(studyTitle: String, model: ConsentViewModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Text(
            text = studyTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoreColors.Primary,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Divider(color = MoreColors.Primary,
            modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = getStringResource(id = R.string.more_permissions_main_consent_text),
            color = MoreColors.Primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(4.dp))

        ConsentListItem(title = "Study Consent",
            description = model.permissionModel.value.studyParticipantInfo)
    }
}