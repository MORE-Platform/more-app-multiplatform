package io.redlink.more.app.android.activities.observationErrors

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.observations.Observation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObservationErrorViewModel : ViewModel() {
    val observationErrors = mutableStateListOf<String>()
    val observationErrorActions = mutableStateListOf<String>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            MoreApplication.shared!!.observationFactory.observationErrors.collect {
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