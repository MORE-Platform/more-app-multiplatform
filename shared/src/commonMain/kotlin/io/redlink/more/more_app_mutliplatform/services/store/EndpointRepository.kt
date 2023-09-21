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

    fun loggingEndpoint(): String = LOGGING_ENDPOINT

    companion object {
        private const val ENDPOINT_KEY = "sharedStorageEndpointKey"
        private const val DATA_BASE_PATH_ENDPOINT: String =
            //"https://data.platform-test.more.redlink.io/api/v1"
            "https://data.more-health.at/api/v1"

        private const val LOGGING_ENDPOINT: String = "https://1d2fb48a3e6e4da6aa8800f5b76711fd.us-central1.gcp.cloud.es.io:443/search-more-logs/_bulk"
    }
}