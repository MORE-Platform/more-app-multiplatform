package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.composables.AccordionWithList
import io.redlink.more.more_app_mutliplatform.android.extensions.formattedString
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.jvmLocalDateTime
import io.redlink.more.more_app_mutliplatform.android.shared_composables.AccordionReadMore
import io.redlink.more.more_app_mutliplatform.android.shared_composables.ActivityProgressView
import io.redlink.more.more_app_mutliplatform.android.shared_composables.BasicText
import io.redlink.more.more_app_mutliplatform.android.shared_composables.HeaderTitle
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.observations.Observation
import org.mongodb.kbson.ObjectId


@Composable
fun StudyDetailsView(navController: NavController, viewModel: StudyDetailsViewModel) {
    viewModel.model.value?.let {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn {
                item {
                    HeaderTitle(title = it.study.studyTitle)
                    Spacer(Modifier.height(12.dp))
                    ActivityProgressView(
                        finishedTasks = it.finishedTasks.toInt(),
                        totalTasks = it.totalTasks.toInt()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicText(text = "${getStringResource(R.string.study_duration)}: ")
                        BasicText(
                            text = "${it.study.start?.epochSeconds?.jvmLocalDateTime()?.formattedString()} - ${
                                it.study.end?.epochSeconds?.jvmLocalDateTime()?.formattedString()
                            }",
                            color = MoreColors.Secondary
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                    AccordionReadMore(
                        title = getStringResource(R.string.participant_information),
                        description = it.study.participantInfo,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    AccordionWithList(
                        title = getStringResource(R.string.observation_modules),
                        observations = it.observations,
                        navController = navController
                    )
                }
            }
        }
    }
}