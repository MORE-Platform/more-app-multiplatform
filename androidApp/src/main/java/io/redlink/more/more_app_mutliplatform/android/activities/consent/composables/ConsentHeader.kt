package io.redlink.more.more_app_mutliplatform.android.activities.consent.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.shared_composables.AccordionReadMore
import io.redlink.more.more_app_mutliplatform.android.shared_composables.HeaderTitle

@Composable
fun ConsentHeader(studyTitle: String, studyParticipantInfo: String) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())) {
        Column(horizontalAlignment = Alignment.Start)
        {
            HeaderTitle(title = studyTitle, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.padding(8.dp))

        AccordionReadMore(description = studyParticipantInfo)
    }
}