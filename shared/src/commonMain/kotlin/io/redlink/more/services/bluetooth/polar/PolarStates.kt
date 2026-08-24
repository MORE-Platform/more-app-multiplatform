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
package io.redlink.more.services.bluetooth.polar

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