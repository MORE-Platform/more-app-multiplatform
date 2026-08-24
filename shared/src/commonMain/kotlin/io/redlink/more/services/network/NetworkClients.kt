/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.services.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.getTimeMillis
import io.ktor.utils.io.core.Closeable
import io.redlink.more.models.CredentialModel
import io.redlink.more.services.network.openapi.api.ConfigurationApi
import io.redlink.more.services.network.openapi.api.DataApi
import io.redlink.more.services.network.openapi.api.GarminRegistrationApi
import io.redlink.more.services.network.openapi.api.NotificationsApi
import io.redlink.more.services.network.openapi.api.RegistrationApi
import io.redlink.more.services.store.CredentialRepository
import io.redlink.more.services.store.EndpointRepository
import kotlinx.serialization.json.Json

class NetworkClients(
    private val credentialRepository: CredentialRepository,
    private val endpointRepository: EndpointRepository,
    private val clientTimeoutMs: Long = 5 * 60 * 1000L // 5 minutes by default
) : Closeable {
    private var httpClient: HttpClient? = null
    private var registrationApi: RegistrationApi? = null
    private var configurationApi: ConfigurationApi? = null
    private var dataApi: DataApi? = null
    private var notificationApi: NotificationsApi? = null
    private var garminRegistrationApi: GarminRegistrationApi? = null
    private var registrationBaseUrl: String? = null

    private var httpClientLastUsed: Long = 0L
    private var registrationLastUsed: Long = 0L
    private var configurationLastUsed: Long = 0L
    private var dataLastUsed: Long = 0L
    private var notificationLastUsed: Long = 0L
    private var garminLastUsed: Long = 0L

    private var lastCredentialsKey: Pair<String, String?>? = null

    private fun currentCredentialsKey(): Pair<String, String?>? =
        credentialRepository.credentials.value?.let { c ->
            c.apiId to c.apiKey
        }

    private fun ensureCredentialsUpToDate() {
        val current = currentCredentialsKey()
        if (current != lastCredentialsKey) {
            Napier.i(tag = "NetworkClients::ensureCredentialsUpToDate") {
                "Credentials changed. Clearing cached HTTP clients & APIs."
            }
            clearData()
            lastCredentialsKey = current
        }
    }

    private fun now(): Long = getTimeMillis()

    private fun isExpired(lastUsed: Long): Boolean =
        lastUsed != 0L && (now() - lastUsed) > clientTimeoutMs

    private fun getHttpClientWithAuth(credentials: CredentialModel? = null): HttpClient? {
        ensureCredentialsUpToDate()

        val baseClient = getHttpClient() ?: return null
        val creds = credentials ?: credentialRepository.credentials.value

        return baseClient.config {
            install(Auth) {
                creds?.let { authCredentials ->
                    basic {
                        credentials {
                            BasicAuthCredentials(
                                username = authCredentials.apiId,
                                password = authCredentials.apiKey
                            )
                        }
                        sendWithoutRequest { true }
                    }
                } ?: run {
                    Napier.i(tag = "NetworkService::getHttpClientWithAuth ") {
                        "No credentials available. Using anonymous client"
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    fun getHttpClient(): HttpClient? {
        ensureCredentialsUpToDate()

        val now = now()
        val current = httpClient

        if (current == null || isExpired(httpClientLastUsed)) {
            current?.close()
            httpClient = getHttpClient(Logger.DEFAULT)
        }

        httpClientLastUsed = now
        return httpClient
    }

    fun getConfigApi(credentials: CredentialModel? = null): ConfigurationApi? {
        ensureCredentialsUpToDate()

        val now = now()
        if (credentials != null || configurationApi == null || isExpired(configurationLastUsed)) {
            configurationApi = getHttpClientWithAuth(credentials)?.let { client ->
                ConfigurationApi(baseUrl(), client)
            }
        }
        configurationLastUsed = now
        return configurationApi
    }

    fun getDataApi(): DataApi? {
        ensureCredentialsUpToDate()

        val now = now()
        if (dataApi == null || isExpired(dataLastUsed)) {
            dataApi = getHttpClientWithAuth()?.let { client ->
                DataApi(baseUrl(), client)
            }
        }
        dataLastUsed = now
        return dataApi
    }

    fun getRegistrationApi(baseUrl: String? = null): RegistrationApi? {
        ensureCredentialsUpToDate()

        val now = now()
        val effectiveBase = baseUrl ?: baseUrl()

        if (
            registrationApi == null ||
            registrationBaseUrl != effectiveBase ||
            isExpired(registrationLastUsed)
        ) {
            registrationApi = getHttpClientWithAuth()?.let { client ->
                RegistrationApi(effectiveBase, client)
            }
            registrationBaseUrl = effectiveBase
        }

        registrationLastUsed = now
        return registrationApi
    }

    fun getNotificationApi(): NotificationsApi? {
        ensureCredentialsUpToDate()

        val now = now()
        if (notificationApi == null || isExpired(notificationLastUsed)) {
            notificationApi = getHttpClientWithAuth()?.let { client ->
                NotificationsApi(baseUrl(), client)
            }
        }
        notificationLastUsed = now
        return notificationApi
    }

    fun getGarminRegistrationApi(): GarminRegistrationApi? {
        ensureCredentialsUpToDate()

        val now = now()
        if (garminRegistrationApi == null || isExpired(garminLastUsed)) {
            garminRegistrationApi = getHttpClientWithAuth()?.let { client ->
                GarminRegistrationApi(baseUrl(), client)
            }
        }
        garminLastUsed = now
        return garminRegistrationApi
    }

    fun basicAuthHeader(): String? {
        ensureCredentialsUpToDate()
        return credentialRepository.credentials.value?.basicAuthHeader()
    }

    fun baseUrl(): String = endpointRepository.endpoint()

    private fun clearData() {
        registrationBaseUrl = null
        registrationApi = null
        configurationApi = null
        dataApi = null
        notificationApi = null
        garminRegistrationApi = null

        httpClient?.close()
        httpClient = null

        Napier.d(tag = "NetworkClients::clearData") { "Cleared the Http engine" }

        httpClientLastUsed = 0L
        registrationLastUsed = 0L
        configurationLastUsed = 0L
        dataLastUsed = 0L
        notificationLastUsed = 0L
        garminLastUsed = 0L
    }

    override fun close() {
        Napier.d(tag = "NetworkClients::close") { "Clearing the Http engine..." }
        clearData()
    }
}