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
package io.redlink.more.models

import io.ktor.util.encodeBase64

data class CredentialModel(val apiId: String, val apiKey: String) {
    fun basicAuthHeader(): String {
        val raw = "$apiId:$apiKey"
        val encoded = raw.encodeBase64()
        return "Basic $encoded"
    }
}
