package io.redlink.more.mocks

import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.ObservationDataManager
import io.redlink.more.observations.ObservationDataManagerImpl


fun mockObservationDataManager(repository: MainRepository = MockMainRepository()): ObservationDataManager {
    return object : ObservationDataManagerImpl(repository) {
        override val konnection: dev.tmapps.konnection.Konnection? = null
        override fun isConnected(): Boolean = false
        override fun sendData(immediately: Boolean, onCompletion: (Boolean) -> Unit) {}
    }
}
