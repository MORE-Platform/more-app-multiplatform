package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.composables.AccordionWithList
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.composables.ObservationList
import io.redlink.more.more_app_mutliplatform.android.extensions.formattedString
import io.redlink.more.more_app_mutliplatform.android.shared_composables.*
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun StudyDetailsView(viewModel: StudyDetailsViewModel) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn {
            item {
                HeaderTitle(title = viewModel.studyTitle.value)
                Spacer(Modifier.height(12.dp))
                ActivityProgressView(
                    finishedTasks = viewModel.finishedTasks.value.toInt(),
                    totalTasks = viewModel.totalTasks.value.toInt()
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicText(text = "Study duration: ")
                    BasicText(
                        text = "${viewModel.start.value.formattedString("dd/MM/yyyy")} - ${
                            viewModel.end.value.formattedString(
                                "dd/MM/yyyy"
                            )
                        }",
                        color = MoreColors.Secondary
                    )
                }
                Spacer(Modifier.height(40.dp))
                AccordionReadMore(
                    title = "Participant Information",
                    description = viewModel.participantInfo.value,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                AccordionWithList(
                    title = "Observation Modules",
                    observations = viewModel.observations.value
                )
            }
        }
    }
}