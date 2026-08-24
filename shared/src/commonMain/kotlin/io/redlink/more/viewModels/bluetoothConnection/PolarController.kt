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
package io.redlink.more.viewModels.bluetoothConnection

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.services.bluetooth.polar.PolarStates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class PolarController(
    repos: MainRepository
) {
    @NativeCoroutines
    val hrFeatureChange: Flow<Pair<Boolean, Boolean>> = repos.study.studyState
        .combine(PolarStates.hrFeatureReady) { studyState, hrReady ->
            Pair(
                studyState.isActive(),
                hrReady
            )
        }
        .distinctUntilChanged()
}