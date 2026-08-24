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
package io.redlink.more.observations

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ObservationStates {
    private val _observationErrors = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    @NativeCoroutines
    val observationErrors: StateFlow<Map<String, Set<String>>> = _observationErrors

    fun updateObservationErrors(observationType: String, errors: Set<String>) {
        if (errors.isEmpty()) {
            _observationErrors.value = _observationErrors.value - observationType
        } else {
            _observationErrors.value = _observationErrors.value + (observationType to errors)
        }
    }

    fun resetAll() {
        _observationErrors.value = emptyMap()
    }
}