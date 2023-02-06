package io.redlink.more.more_app_mutliplatform.services.store

import io.redlink.more.more_app_mutliplatform.models.CredentialModel

class CredentialRepository(private val sharedStorageRepository: SharedStorageRepository) {
    private var cache: CredentialModel? = null

    init {
        cache = load()
    }

    fun store(credentials: CredentialModel): Boolean {
        if (credentials.apiId.isNotEmpty() && credentials.apiKey.isNotEmpty()) {
            sharedStorageRepository.storeSecure(CREDENTIAL_ID, credentials.apiId)
            sharedStorageRepository.storeSecure(CREDENTIAL_KEY, credentials.apiKey)
            cache = credentials
            return true
        }
        return false
    }

    private fun load(): CredentialModel? {
        val apiId = sharedStorageRepository.loadSecure(CREDENTIAL_ID, "")
        val apiKey = sharedStorageRepository.loadSecure(CREDENTIAL_KEY, "")
        if (apiId.isNotEmpty() && apiKey.isNotEmpty()) {
            return CredentialModel(apiId, apiKey)
        }
        return null
    }

    fun credentials() = cache

    fun hasCredentials() = cache != null

    companion object {
        private const val CREDENTIAL_ID = "sharedStorageCredentialID"
        private const val CREDENTIAL_KEY = "sharedStorageCredentialKey"
    }
}