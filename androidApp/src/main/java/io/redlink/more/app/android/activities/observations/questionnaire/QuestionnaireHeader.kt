package io.redlink.more.app.android.activities.observations.questionnaire

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.shared_composables.HeaderDescription
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.R


@Composable
fun QuestionnaireHeader(model: QuestionnaireViewModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(2.dp))
    {
        HeaderTitle(title = model.observationTitle.value)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .heightIn(max = 100.dp)
                .fillMaxWidth()
        ) {
            item {
                Text(
                    text = stringResource(R.string.more_questionnaire_type),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MoreColors.Primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            item {
                HeaderDescription(description = model.observationParticipantInfo.value)
            }
        }

        Spacer(Modifier.height(8.dp))

        Divider(Modifier.height(1.dp))
    }
}