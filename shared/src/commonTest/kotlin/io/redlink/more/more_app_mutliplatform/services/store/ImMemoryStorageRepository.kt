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
package io.redlink.more.more_app_mutliplatform.services.store

class ImMemoryStorageRepository : SharedStorageRepository {

    private val storageMap = HashMap<String, Any>()

    override fun store(key: String, value: String) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Boolean) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Int) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Float) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Double) {
        storageMap[key] = value
    }

    override fun store(key: String, value: Long) {
        storageMap[key] = value
    }

    override fun load(key: String, default: String): String {
        return storageMap[key] as? String ?: default
    }

    override fun load(key: String, default: Boolean): Boolean {
        return storageMap[key] as? Boolean ?: default
    }

    override fun load(key: String, default: Int): Int {
        return storageMap[key] as? Int ?: default
    }

    override fun load(key: String, default: Float): Float {
        return storageMap[key] as? Float ?: default
    }

    override fun load(key: String, default: Double): Double {
        return storageMap[key] as? Double ?: default
    }

    override fun load(key: String, default: Long): Long {
        return storageMap[key] as? Long ?: default
    }

    override fun remove(key: String) {
        storageMap.remove(key)
    }
}