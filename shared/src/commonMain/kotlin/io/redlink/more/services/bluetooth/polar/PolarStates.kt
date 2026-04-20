package io.redlink.more.services.bluetooth.polar

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PolarStates {
    private val _hrFeatureReady = MutableStateFlow(true)
    private val _sdkModeReady = MutableStateFlow(false)

    @NativeCoroutines
    val hrFeatureReady: StateFlow<Boolean> = _hrFeatureReady

    @NativeCoroutines
    val sdkModeReady: StateFlow<Boolean> = _sdkModeReady

    fun hrFeatureReady(ready: Boolean) {
        _hrFeatureReady.value = ready
    }

    fun sdkModeReady(ready: Boolean) {
        _sdkModeReady.value = ready
    }

    fun resetAll() {
        _hrFeatureReady.value = false
        _sdkModeReady.value = false
    }
}