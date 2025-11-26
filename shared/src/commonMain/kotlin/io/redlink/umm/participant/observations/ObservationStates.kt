package io.redlink.umm.participant.observations

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