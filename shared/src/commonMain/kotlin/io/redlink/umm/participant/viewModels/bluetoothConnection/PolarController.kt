package io.redlink.umm.participant.viewModels.bluetoothConnection

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.services.bluetooth.polar.PolarStates
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