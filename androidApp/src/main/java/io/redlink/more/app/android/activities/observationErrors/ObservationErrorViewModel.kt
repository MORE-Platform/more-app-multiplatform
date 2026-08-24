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

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.redlink.more.observations.Observation.Companion.ERROR_DEVICE_NOT_CONNECTED
import io.redlink.more.observations.ObservationStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObservationErrorViewModel : ViewModel() {
    val observationErrors = mutableStateListOf<String>()
    val observationErrorActions = mutableStateListOf<String>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            ObservationStates.observationErrors.collect {
                Napier.d { it.toString() }
                val (actions, errors) = it.values.flatten().toSet()
                    .partition { it == ERROR_DEVICE_NOT_CONNECTED }
                withContext(Dispatchers.Main) {
                    observationErrors.clear()
                    observationErrors.addAll(errors)
                    observationErrorActions.clear()
                    observationErrorActions.addAll(actions)
                }
            }
        }
    }

}