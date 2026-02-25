package io.redlink.more.app.android.activities.observationErrors

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.redlink.more.observations.Observation
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
                    .partition { it == Observation.ERROR_DEVICE_NOT_CONNECTED }
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