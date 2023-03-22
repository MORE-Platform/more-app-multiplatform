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

    companion object {
        private const val CREDENTIAL_ID = "sharedStorageCredentialID"
        private const val CREDENTIAL_KEY = "sharedStorageCredentialKey"
    }
}