package io.redlink.more.app.android.activities.studyStates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.MediumTitle
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.Title
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun StudyUpdateView() {
    val progressSize = 50.dp
    MoreBackground {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Title(
                text = "${getStringResource(id = R.string.study_update)}!",
                textAlign = TextAlign.Center
            )
            MediumTitle(
                text = "${getStringResource(id = R.string.study_update_wait)}!",
                textAlign = TextAlign.Center
            )
            CircularProgressIndicator(
                color = MoreColors.Primary,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .width(progressSize)
                    .height(progressSize)
                    .padding(vertical = 8.dp)
            )
        }

    }
}

@Preview
@Composable
fun StudyUpdatePreview() {
    StudyUpdateView()
}