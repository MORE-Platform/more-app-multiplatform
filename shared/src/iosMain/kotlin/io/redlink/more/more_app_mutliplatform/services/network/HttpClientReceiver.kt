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
package io.redlink.more.more_app_mutliplatform.services.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

actual fun getHttpClient(customLogger: Logger): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) {
        json()
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        request {
            contentType(ContentType.Application.Json)
        }
        Logging {
            logger = customLogger
            level = LogLevel.ALL
        }
        Auth {
            basic {
            }
        }
    }
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}