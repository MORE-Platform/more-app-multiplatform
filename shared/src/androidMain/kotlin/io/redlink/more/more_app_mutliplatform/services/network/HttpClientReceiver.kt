package io.redlink.more.more_app_mutliplatform.services.network

import io.ktor.client.*
import io.ktor.client.engine.android.*

actual fun getHttpClient(): HttpClient = HttpClient(Android) {}