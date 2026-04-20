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

import io.redlink.more.util.validateAndNormalizeUrl

class EndpointRepositoryImpl(private val sharedStorageRepository: SharedStorageRepository) :
    EndpointRepository {
    private var cache: String = ""

    init {
        cache = loadEndpoint()
    }

    override fun storeEndpoint(endpoint: String) {
        val validEndpoint = endpoint.validateAndNormalizeUrl()?.ifBlank { DATA_BASE_PATH_ENDPOINT }
            ?: DATA_BASE_PATH_ENDPOINT
        sharedStorageRepository.store(ENDPOINT_KEY, validEndpoint)
        cache = validEndpoint
    }

    private fun loadEndpoint(): String {
        return sharedStorageRepository.load(ENDPOINT_KEY, cache)
    }

    override fun removeEndpoint() {
        cache = ""
        sharedStorageRepository.remove(ENDPOINT_KEY)
    }

    override fun endpoint(): String = "https://data.more-health.at/api/v1"

    companion object {
        private const val ENDPOINT_KEY = "sharedStorageEndpointKey"
        private const val DATA_BASE_PATH_ENDPOINT: String =
            "https://data.more-health.at/api/v1"
    }
}
