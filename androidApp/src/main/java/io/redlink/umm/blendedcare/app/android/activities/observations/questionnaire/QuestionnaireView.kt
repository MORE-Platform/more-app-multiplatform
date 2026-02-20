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
package io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.activities.NavigationScreen
import io.redlink.umm.blendedcare.app.android.activities.observations.questionnaire.questionType.QuestionnaireQuestionAnswer
import io.redlink.umm.blendedcare.app.android.extensions.getStringResource
import io.redlink.umm.blendedcare.app.android.shared_composables.ErrorMessage
import io.redlink.umm.participant.models.QuestionType

/**
 * Saves the *answer value* (not the question). Extend this when you add new answer shapes.
 *
 * Supported:
 * - SINGLE_CHOICE -> String
 * - MULTIPLE_CHOICE -> List<String>
 */
private val AnswerValueSaver: Saver<Any?, Bundle> = Saver(
    save = { answer ->
        when (answer) {
            null -> bundleOf("t" to "null")
            is String -> bundleOf("t" to "s", "v" to answer)
            is List<*> -> bundleOf(
                "t" to "l",
                "v" to answer.filterIsInstance<String>().toTypedArray()
            )

            else -> bundleOf("t" to "null")
        }
    },
    restore = { b ->
        when (b.getString("t")) {
            "s" -> b.getString("v")
            "l" -> (b.getStringArray("v") ?: emptyArray()).toList()
            else -> null
        }
    }
)

@Composable
fun QuestionnaireView(navController: NavController, viewModel: QuestionViewModel) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(
        NavigationScreen.Question.routeWithParameters()
    )

    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }

    DisposableEffect(route) {
        onDispose { viewModel.viewDidDisappear() }
    }

    var selectedAnswer by rememberSaveable(route, stateSaver = AnswerValueSaver) {
        mutableStateOf(null)
    }

    if (viewModel.hasData.value) {
        val observation by viewModel.coreViewModel.questionModel.collectAsStateWithLifecycle(null)
        val type = observation?.type ?: QuestionType.NON

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            QuestionnaireQuestionAnswer(
                model = viewModel,
                selectedAnswer = selectedAnswer,
                onAnswerSelected = { selectedAnswer = it }
            )
        }

        QuestionnaireButtons(
            questionType = type,
            selectedAnswer = selectedAnswer,
            onFinish = {
                viewModel.finish(type, it)
                navController.navigate(NavigationScreen.QUESTIONNAIRE_RESPONSE.routeWithParameters())
            }
        )
    } else {
        ErrorMessage(message = "${getStringResource(id = R.string.data_not_found)}!")
    }
}