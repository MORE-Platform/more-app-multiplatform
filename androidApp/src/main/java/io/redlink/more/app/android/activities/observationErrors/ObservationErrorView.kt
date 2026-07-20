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

package io.redlink.more.app.android.activities.observationErrors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun ObservationErrorView() {
    val viewModel = remember {
        ObservationErrorViewModel()
    }
    ObservationErrorListView(
        errors = viewModel.observationErrors,
        errorActions = viewModel.observationErrorActions
    )
}