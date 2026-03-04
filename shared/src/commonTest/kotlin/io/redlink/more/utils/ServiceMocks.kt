package io.redlink.more.utils

import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.ObservationDataManager


fun mockObservationDataManager(repository: MainRepository = MockMainRepository()): ObservationDataManager {
    return object : ObservationDataManager(repository) {
        override val konnection: dev.tmapps.konnection.Konnection? = null
        override fun isConnected(): Boolean = false
        override fun sendData(immediately: Boolean, onCompletion: (Boolean) -> Unit) {}
    }
}
