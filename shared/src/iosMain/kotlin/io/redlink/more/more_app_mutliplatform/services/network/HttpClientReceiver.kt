package io.redlink.more.more_app_mutliplatform.services.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

actual fun getHttpClient(): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) {
        json()
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        request {
            contentType(ContentType.Application.Json)
        }
        Logging {
            logger = Logger.DEFAULT
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