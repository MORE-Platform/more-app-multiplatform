package io.redlink.more.more_app_mutliplatform.services.store

class EndpointRepository(private val sharedStorageRepository: SharedStorageRepository) {
    private var cache: String = ""

    init {
        cache = loadEndpoint()
    }

    fun storeEndpoint(endpoint: String) {
        sharedStorageRepository.store(ENDPOINT_KEY, endpoint)
        cache = endpoint
    }

    private fun loadEndpoint(): String {
        return sharedStorageRepository.load(ENDPOINT_KEY, cache)
    }

    fun endpoint(): String? = cache.ifEmpty { null }

    companion object {
        private const val ENDPOINT_KEY = "sharedStorageEndpointKey"
    }
}