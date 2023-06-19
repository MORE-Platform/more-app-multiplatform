package io.redlink.more.app.android.activities.studyDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.studyDetails.composables.AccordionWithList
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDateTime
import io.redlink.more.app.android.shared_composables.AccordionReadMore
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarView
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.more.app.android.extensions.toDate
import io.redlink.more.app.android.shared_composables.TimeframeDays
import org.mongodb.kbson.ObjectId
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


@Composable
fun StudyDetailsView(navController: NavController, viewModel: StudyDetailsViewModel, taskCompletionBarViewModel: TaskCompletionBarViewModel) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.STUDY_DETAILS.route)
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }

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
                    TaskCompletionBarView(taskCompletionBarViewModel)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (it.study.start?.epochSeconds != null && it.study.end?.epochSeconds != null) {
                            BasicText(text = "${getStringResource(R.string.study_duration)}: ")
                            BasicText(
                                text = "${it.study.start!!.epochSeconds.jvmLocalDateTime().formattedString()} - ${
                                    it.study.end!!.epochSeconds.jvmLocalDateTime().formattedString()
                                }",
                                color = MoreColors.Secondary
                            )
                        }

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