package io.redlink.more.more_app_mutliplatform.services.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger

expect fun getHttpClient(customLogger: Logger = Logger.DEFAULT): HttpClient