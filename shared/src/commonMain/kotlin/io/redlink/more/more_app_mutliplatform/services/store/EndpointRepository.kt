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

    fun removeEndpoint() {
        cache = ""
        sharedStorageRepository.remove(ENDPOINT_KEY)
    }

    fun endpoint(): String = cache.ifEmpty { DATA_BASE_PATH_ENDPOINT }

    companion object {
        private const val ENDPOINT_KEY = "sharedStorageEndpointKey"
        private const val DATA_BASE_PATH_ENDPOINT: String =
            //"https://data.platform-test.more.redlink.io/api/v1"
            "https://moredata.ngrok.io/api/v1"
    }
}