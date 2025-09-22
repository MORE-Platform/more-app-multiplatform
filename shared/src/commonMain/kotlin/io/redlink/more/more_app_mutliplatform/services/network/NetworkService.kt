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

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.models.LoginModel
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.AppConfiguration
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Error
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotification
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotificationToken
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyConsent
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import kotlinx.serialization.json.Json

private const val TAG = "NetworkService"

class NetworkService(
    private val endpointRepository: EndpointRepository,
    private val credentialRepository: CredentialRepository,
) : Closeable {
    private var httpClient: HttpClient? = null

    private var engineUseCounter = 10

    private fun initHttpClientWithAuth(): HttpClient? {
        initHttpClient()
        val creds = credentialRepository.credentials.value
        return httpClient?.config {
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

    private fun initHttpClient() {
        if (httpClient == null) {
            httpClient = getHttpClient(Logger.DEFAULT)
        }
    }

    suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?> {
        try {
            credentialRepository.credentials.value?.let { credentials ->
                Napier.i(tag = "NetworkService::deleteParticipation") { "Deleting Participation..." }
                val client = initHttpClientWithAuth() ?: return Pair(
                    false,
                    NetworkServiceError(null, "Failed to init HTTP client")
                )
                val baseUrl = endpointRepository.endpoint()

                val response = client.delete("$baseUrl/registration")

                Napier.i(response.toString(), tag = TAG)
                close()
                if (response.status.isSuccess()) {
                    Napier.i(tag = "NetworkService::deleteParticipation") { "Participation deleted!" }
                    return Pair(true, null)
                }
                Napier.e(tag = "NetworkService::deleteParticipation") { "Error; Code: ${response.status.value}" }
                val error = createErrorBody(response.status.value, response)
                return Pair(false, error)
            }
            return Pair(false, NetworkServiceError(null, "No credentials"))
        } catch (err: Exception) {
            Napier.e(tag = "NetworkService::deleteParticipation") { err.stackTraceToString() }
            return Pair(false, getException(err))
        }
    }

    suspend fun validateRegistrationToken(loginModel: LoginModel): Pair<Study?, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::validateRegistrationToken") { "Validating Registration token..." }
            initHttpClient()
            val baseUrl = loginModel.endpoint ?: endpointRepository.endpoint()
            val client = httpClient ?: return Pair(
                null,
                NetworkServiceError(null, "HTTP client not initialized")
            )

            val response = client.get("$baseUrl/registration") {
                header("More-Registration-Token", loginModel.token)
                contentType(ContentType.Application.Json)
            }

            Napier.i(response.toString(), tag = TAG)
            if (response.status.isSuccess()) {
                val study: Study = response.body()
                Napier.i(tag = "NetworkService::validateRegistrationToken") { "Registration token valid!" }
                return Pair(study, null)
            }
            val error = createErrorBody(response.status.value, response)
            return Pair(null, error)

        } catch (err: Exception) {
            Napier.e(tag = "NetworkService::validateRegistrationToken") { err.stackTraceToString() }
            return Pair(null, getException(err))
        }
    }

    suspend fun sendConsent(
        loginModel: LoginModel, studyConsent: StudyConsent
    ): Pair<AppConfiguration?, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::sendConsent") { "Sending Consent..." }
            initHttpClient()
            val baseUrl = loginModel.endpoint ?: endpointRepository.endpoint()
            val client = httpClient ?: return Pair(
                null,
                NetworkServiceError(null, "HTTP client not initialized")
            )

            val response = client.post("$baseUrl/registration") {
                header("More-Registration-Token", loginModel.token)
                contentType(ContentType.Application.Json)
                setBody(studyConsent)
            }

            if (response.status.isSuccess()) {
                val appConfig: AppConfiguration = response.body()
                Napier.i(tag = "NetworkService::sendConsent") { "Credentials received!" }
                return Pair(appConfig, null)
            }
            return Pair(null, createErrorBody(response.status.value, response))
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::sendConsent") { e.stackTraceToString() }
            return Pair(null, getException(e))
        }
    }

    suspend fun getStudyConfig(): Pair<Study?, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::getStudyConfig") { "Downloading study data..." }
            credentialRepository.credentials.value?.let { credentials ->
                val client = initHttpClientWithAuth() ?: return Pair(
                    null,
                    NetworkServiceError(null, "Failed to init HTTP client")
                )
                val baseUrl = endpointRepository.endpoint()

                val response = client.get("$baseUrl/config/study") {
                    contentType(ContentType.Application.Json)
                }

                if (response.status.isSuccess()) {
                    val study: Study = response.body()
                    Napier.i(tag = "NetworkService::getStudyConfig") { "Loading study data success!" }
                    return Pair(study, null)
                }
                return Pair(null, createErrorBody(response.status.value, response))
            }
            return Pair(null, NetworkServiceError(null, "No credentials set!"))
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::getStudyConfig") { e.stackTraceToString() }
            return Pair(null, getException(e))
        }
    }

    suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::sendNotificationToken") { "Sending notification token..." }
            credentialRepository.credentials.value?.let { credentials ->
                val client = initHttpClientWithAuth() ?: return Pair(
                    false,
                    NetworkServiceError(null, "Failed to init HTTP client")
                )
                val baseUrl = endpointRepository.endpoint()
                val pushToken = PushNotificationToken(token = token)

                val response = client.put("$baseUrl/config/notifications/FCM") {
                    contentType(ContentType.Application.Json)
                    setBody(pushToken)
                }

                if (response.status.isSuccess()) {
                    Napier.i(tag = "NetworkService::sendNotificationToken") { "Uploading notification token success!" }
                    return Pair(true, null)
                }
                return Pair(false, createErrorBody(response.status.value, response))
            }
            return Pair(false, NetworkServiceError(null, "No credentials found!"))
        } catch (err: Exception) {
            Napier.e(tag = "NetworkService::sendNotificationToken") { err.stackTraceToString() }
            return Pair(false, getException(err))
        }
    }

    suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::sendData") { "Sending bulk ${data.bulkId} with ${data.dataPoints.size} datapoints with first being ${data.dataPoints.first()}..." }
            credentialRepository.credentials.value?.let { credentials ->
                val client = initHttpClientWithAuth() ?: return Pair(
                    emptySet(),
                    NetworkServiceError(null, "Failed to init HTTP client")
                )
                val baseUrl = endpointRepository.endpoint()

                val response = client.post("$baseUrl/data/bulk") {
                    contentType(ContentType.Application.Json)
                    setBody(data)
                }

                if (response.status.isSuccess()) {
                    val result: List<String> = response.body()
                    Napier.i(tag = "NetworkService::sendData") { "Sent data!" }
                    return Pair(result.toSet(), null)
                }
                return Pair(emptySet(), createErrorBody(response.status.value, response))
            }
            return Pair(emptySet(), NetworkServiceError(null, "No credentials set!"))
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::sendData") { e.stackTraceToString() }
            return Pair(emptySet(), getException(e))
        }
    }

    suspend fun downloadMissedNotifications(): List<PushNotification> {
        return try {
            Napier.d(tag = "NetworkService::downloadMissedNotifications") { "Downloading missed notifications from the Server..." }
            credentialRepository.credentials.value?.let { credentials ->
                val client = initHttpClientWithAuth() ?: return@let emptyList()
                val baseUrl = endpointRepository.endpoint()

                val response = client.get("$baseUrl/notifications") {
                    contentType(ContentType.Application.Json)
                }

                if (response.status.isSuccess()) {
                    val notifications: List<PushNotification> = response.body()
                    Napier.d(tag = "NetworkService::downloadMissedNotifications") { "Downloaded Messages list: $notifications" }
                    notifications
                } else {
                    Napier.d(tag = "NetworkService::downloadMissedNotifications") { "No notifications received from the server" }
                    emptyList()
                }
            } ?: run {
                Napier.d(tag = "NetworkService::downloadMissedNotifications") { "No credentials available" }
                emptyList()
            }
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::downloadMissedNotifications") { "Notification List error: $e" }
            emptyList()
        }
    }

    suspend fun deletePushNotification(msgId: String) {
        try {
            credentialRepository.credentials.value?.let { credentials ->
                val client = initHttpClientWithAuth() ?: return
                val baseUrl = endpointRepository.endpoint()

                val response = client.delete("$baseUrl/notifications/$msgId") {
                    contentType(ContentType.Application.Json)
                }

                if (response.status.isSuccess()) {
                    Napier.d(tag = "NetworkService::deletePushNotification") { "Successfully deleted notification with id: $msgId" }
                } else {
                    Napier.d(tag = "NetworkService::deletePushNotification") { "Push notification not found with msgID: $msgId. Could not delete!" }
                }
            }
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::deletePushNotification") { "Notification deletion error: $e" }
        }
    }

    private suspend fun createErrorBody(
        code: Int,
        responseBody: HttpResponse?
    ): NetworkServiceError {
        return try {
            if (responseBody == null) {
                return NetworkServiceError(code = code, message = "Error")
            }
            val error: Error? = try {
                responseBody.body<Error>()
            } catch (_: Exception) {
                null
            }

            NetworkServiceError(code = code, message = error?.msg ?: "Error")
        } catch (e: Exception) {
            getException(e, code)
        }
    }

    private fun getException(exception: Exception, code: Int? = null): NetworkServiceError {
        val errorResponse = "System error!"
        Napier.e("Exception: ${exception.stackTraceToString()}", tag = TAG)
        exception.printStackTrace()
        return NetworkServiceError(code, errorResponse)
    }

    override fun close() {
        Napier.d(tag = "NetworkService::close") { "Clearing the Http engine..." }
        engineUseCounter = 10
        httpClient?.close()
        httpClient = null
    }

}