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
package io.redlink.more.services.store

import io.redlink.more.extensions.mapState
import io.redlink.more.models.CredentialModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CredentialRepositoryImpl(private val sharedStorageRepository: SharedStorageRepository) :
    CredentialRepository {
    private val _credentialsLoaded = MutableStateFlow(false)

    override val credentialsLoaded: StateFlow<Boolean> = _credentialsLoaded
    private var _cache = MutableStateFlow<CredentialModel?>(null)

    override val credentials: StateFlow<CredentialModel?> = _cache

    override val hasCredentials: StateFlow<Boolean> =
        credentials.mapState(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate), false) {
            it != null
        }

    init {
        _cache.value = load()
        _credentialsLoaded.value = true
    }

    override fun store(credentials: CredentialModel): Boolean {
        if (credentials.apiId.isNotEmpty() && credentials.apiKey.isNotEmpty()) {
            sharedStorageRepository.store(CREDENTIAL_ID, credentials.apiId)
            sharedStorageRepository.store(CREDENTIAL_KEY, credentials.apiKey)
            _cache.value = credentials
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

    override fun remove() {
        sharedStorageRepository.remove(CREDENTIAL_ID)
        sharedStorageRepository.remove(CREDENTIAL_KEY)
        _cache.value = null
    }

    companion object {
        private const val CREDENTIAL_ID = "sharedStorageCredentialID"
        private const val CREDENTIAL_KEY = "sharedStorageCredentialKey"
    }
}
