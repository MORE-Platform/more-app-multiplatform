/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.mocks

import dev.tmapps.konnection.Konnection
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.ObservationDataManager


import io.redlink.more.services.store.SharedStorageRepository

fun mockObservationDataManager(repository: MainRepository = MockMainRepository()): ObservationDataManager {
    return object : ObservationDataManager(repository) {
        override val konnection: Konnection? = null
        override fun isConnected(): Boolean = false
        override fun sendData(immediately: Boolean, onCompletion: (Boolean) -> Unit) {}
    }
}

class MockSharedStorageRepository : SharedStorageRepository {
    private val data = mutableMapOf<String, Any>()

    override fun store(key: String, value: String) {
        data[key] = value
    }

    override fun store(key: String, value: Boolean) {
        data[key] = value
    }

    override fun store(key: String, value: Int) {
        data[key] = value
    }

    override fun store(key: String, value: Float) {
        data[key] = value
    }

    override fun store(key: String, value: Double) {
        data[key] = value
    }

    override fun store(key: String, value: Long) {
        data[key] = value
    }

    override fun load(key: String, default: String): String = data[key] as? String ?: default
    override fun load(key: String, default: Boolean): Boolean = data[key] as? Boolean ?: default
    override fun load(key: String, default: Int): Int = data[key] as? Int ?: default
    override fun load(key: String, default: Float): Float = data[key] as? Float ?: default
    override fun load(key: String, default: Double): Double = data[key] as? Double ?: default
    override fun load(key: String, default: Long): Long = data[key] as? Long ?: default

    override fun remove(key: String) {
        data.remove(key)
    }
}
