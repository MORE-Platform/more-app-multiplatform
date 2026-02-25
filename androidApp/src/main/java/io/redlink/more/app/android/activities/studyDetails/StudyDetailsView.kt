/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.studyDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.redlink.io.more.app.android.R
import io.redlink.more.app.android.activities.studyDetails.composables.AccordionWithList
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarView
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.more.app.android.extensions.formattedString
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDateTime
import io.redlink.more.app.android.shared_composables.AccordionReadMore
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.theme.MoreColors

@Composable
fun StudyDetailsView(
    navController: NavController,
    taskCompletionBarViewModel: TaskCompletionBarViewModel
) {
    val viewModel = remember { StudyDetailsViewModel() }
    val studyInfo by viewModel.coreViewModel.studyModel.collectAsStateWithLifecycle()

    studyInfo?.let {
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
                        if (it.study.start != null && it.study.end != null) {
                            BasicText(text = "${getStringResource(R.string.study_duration)}: ")
                            BasicText(
                                text = "${
                                    it.study.start!!.jvmLocalDateTime().formattedString()
                                } - ${
                                    it.study.end!!.jvmLocalDateTime().formattedString()
                                }",
                                color = MoreColors.Companion.Secondary
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