package io.redlink.more.more_app_mutliplatform.services.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

actual fun getHttpClient(customLogger: Logger): HttpClient = HttpClient(Android) {
    install(ContentNegotiation) {
        json()
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        Logging {
            logger = customLogger
            level = LogLevel.ALL
        }
    }
}