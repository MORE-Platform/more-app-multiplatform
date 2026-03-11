package io.redlink.more.mocks

import dev.tmapps.konnection.Konnection
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.ObservationDataManager


fun mockObservationDataManager(repository: MainRepository = MockMainRepository()): ObservationDataManager {
    return object : ObservationDataManager(repository) {
        override val konnection: Konnection? = null
        override fun isConnected(): Boolean = false
        override fun sendData(immediately: Boolean, onCompletion: (Boolean) -> Unit) {}
    }
}
