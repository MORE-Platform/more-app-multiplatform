/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.services.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

actual fun getHttpClient(customLogger: Logger): HttpClient = HttpClient(Darwin) {

    defaultRequest {
        contentType(ContentType.Application.Json)
        headers.append("Accept", "application/json")
    }

    install(Logging) {
        logger = customLogger
        level = LogLevel.INFO
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }

    install(Auth)

    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
        configureSession {
            timeoutIntervalForRequest = 30.0
            timeoutIntervalForResource = 60.0
        }
    }
}