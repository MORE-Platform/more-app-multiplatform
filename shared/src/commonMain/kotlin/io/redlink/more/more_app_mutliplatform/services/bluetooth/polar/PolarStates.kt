package io.redlink.more.more_app_mutliplatform.services.bluetooth.polar

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PolarStates {
    private val _hrFeatureReady = MutableStateFlow(true)

    @NativeCoroutines
    val hrFeatureReady: StateFlow<Boolean> = _hrFeatureReady

    fun hrFeatureReady(ready: Boolean) {
        _hrFeatureReady.value = ready
    }

    fun resetAll() {
        _hrFeatureReady.value = false
    }
}