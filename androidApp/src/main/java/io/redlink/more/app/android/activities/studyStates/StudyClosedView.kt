package io.redlink.more.app.android.activities.studyStates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.Heading
import io.redlink.more.app.android.shared_composables.MediumTitle
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.Title
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun StudyClosedView() {
    var loading by remember { mutableStateOf(false) }
    MoreBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Title(
                            text = "${getStringResource(id = R.string.study_closed)}!",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        MediumTitle(text = "${getStringResource(id = R.string.study_closed_thanks)}!")
                    }
                    MoreDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Heading(text = "${getStringResource(id = R.string.study_personal_message)}:")
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    BasicText(text = "Here should be the personal message by the study operator")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    SmallTextButton(
                        text = getStringResource(id = R.string.more_settings_exit_dialog_title)
                    ) {
                        loading = true
                        MoreApplication.shared!!.exitStudy {
                            loading = false
                        }
                    }
                }
            }
        }

    }
}

@Preview
@Composable
fun StudyClosedPreview() {
    StudyClosedView()
}