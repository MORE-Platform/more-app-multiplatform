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

import io.redlink.more.more_app_mutliplatform.models.CredentialModel

class CredentialRepository(private val sharedStorageRepository: SharedStorageRepository) {
    private var cache: CredentialModel? = null

    init {
        cache = load()
    }

    fun store(credentials: CredentialModel): Boolean {
        if (credentials.apiId.isNotEmpty() && credentials.apiKey.isNotEmpty()) {
            sharedStorageRepository.store(CREDENTIAL_ID, credentials.apiId)
            sharedStorageRepository.store(CREDENTIAL_KEY, credentials.apiKey)
            cache = credentials
            return true
        }
        return false
    }

    private fun load(): CredentialModel? {
        val apiId = sharedStorageRepository.load(CREDENTIAL_ID, "")
        val apiKey = sharedStorageRepository.load(CREDENTIAL_KEY, "")
        if (apiId.isNotEmpty() && apiKey.isNotEmpty()) {
            return CredentialModel(apiId, apiKey)
        }
        return null
    }

    fun remove() {
        sharedStorageRepository.remove(CREDENTIAL_ID)
        sharedStorageRepository.remove(CREDENTIAL_KEY)
        cache = null
    }

    fun credentials() = cache ?: load()

    fun hasCredentials() = credentials() != null

    fun loggingKey() = LOGGING_KEY

    companion object {
        private const val CREDENTIAL_ID = "sharedStorageCredentialID"
        private const val CREDENTIAL_KEY = "sharedStorageCredentialKey"
        private const val LOGGING_KEY = "SklEUHRZa0IzX2ZSUXZEWERqZng6MXNzQlZyNE9Ud0NqNGtvVFlwZXFiQQ=="
    }
}