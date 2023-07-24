package io.redlink.more.app.android.activities.studyStates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.MediumTitle
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.Title

@Composable
fun StudyPausedView() {
    MoreBackground() {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            Title(text = "${getStringResource(id = R.string.study_paused)}!", textAlign = TextAlign.Center)
            MediumTitle(text = "${getStringResource(id = R.string.study_paused_descr)}!", textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
fun StudyPausedPreview() {
    StudyPausedView()
}