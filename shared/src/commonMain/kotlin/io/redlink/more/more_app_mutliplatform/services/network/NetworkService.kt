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
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.core.Closeable
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.ConfigurationApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.DataApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.NotificationApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.RegistrationApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.AppConfiguration
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Error
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotification
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotificationServiceType
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotificationToken
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyConsent
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "NetworkService"

class NetworkService(
    private val endpointRepository: EndpointRepository,
    private val credentialRepository: CredentialRepository,
) : Closeable {
    private var httpClient: HttpClient? = null
    private var configurationApi: ConfigurationApi? = null
    private var dataApi: DataApi? = null
    private var notificationApi: NotificationApi? = null

    private var engineUseCounter = 10

    private fun initConfigApi() {
        if (configurationApi == null) {
            Napier.i(tag = "NetworkService::initConfigApi") { "Initializing ConfigurationApi..." }
            initHttpClient()
            credentialRepository.credentials()?.let { credentials ->
                httpClient?.let { httpClient ->
                    val url = endpointRepository.endpoint()
                    configurationApi = ConfigurationApi(baseUrl = url,
                        httpClient.engine,
                        httpClientConfig = { httpClient.engineConfig })
                    configurationApi?.setUsername(credentials.apiId)
                    configurationApi?.setPassword(credentials.apiKey)
                }
            }
        }
    }

    private fun initDataApi() {
        if (dataApi == null || dataApi == null) {
            Napier.i(tag = "NetworkService::initDataApi") { "Init DataAPI..." }
            initHttpClient()
            credentialRepository.credentials()?.let { credentials ->
                httpClient?.let { httpClient ->
                    val api = DataApi(baseUrl = endpointRepository.endpoint(),
                        httpClient.engine,
                        httpClientConfig = { httpClient.engineConfig })
                    api.setUsername(credentials.apiId)
                    api.setPassword(credentials.apiKey)
                    dataApi = api
                }
            }
        }
    }

    private fun initNotificationApi() {
        if (notificationApi == null) {
            Napier.i(tag = "NetworkService::initNotificationApi") { "Init Notification API..." }
            initHttpClient()
            credentialRepository.credentials()?.let { credentials ->
                httpClient?.let { httpClient ->
                    val api = NotificationApi(endpointRepository.endpoint(),
                        httpClient.engine,
                        httpClientConfig = { httpClient.engineConfig })
                    api.setUsername(credentials.apiId)
                    api.setPassword(credentials.apiKey)
                    notificationApi = api
                }
            }
        }
    }

    private fun initLoggingApi() {
        initHttpClient()
    }

    private fun initHttpClient() {
        if (httpClient == null) {
            httpClient = getHttpClient(Logger.DEFAULT)
        }
    }

    suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?> {
        try {
            credentialRepository.credentials()?.let {
                Napier.i(tag = "NetworkService::deleteParticipation") { "Deleting Participation..." }
                val httpClient = getHttpClient()
                val url = endpointRepository.endpoint()
                val registrationApi =
                    RegistrationApi(baseUrl = url, httpClientEngine = httpClient.engine)

                registrationApi.setUsername(it.apiId)
                registrationApi.setPassword(it.apiKey)

                val registrationResponse = registrationApi.unregisterFromStudy() ?: return Pair(
                    false, NetworkServiceError(0, "Response null")
                )
                Napier.i(registrationResponse.response.toString(), tag = TAG)
                close()
                if (registrationResponse.success) {
                    Napier.i(tag = "NetworkService::deleteParticipation") { "Participation deleted!" }
                    return Pair(true, null)
                }
                Napier.e(tag = "NetworkService::deleteParticipation") { "Error; Code: ${registrationResponse.response.status.value}" }
                val error = createErrorBody(
                    registrationResponse.response.status.value, registrationResponse.response
                )
                return Pair(
                    false, error
                )
            }
            return Pair(false, NetworkServiceError(null, "No credentials"))
        } catch (err: Exception) {
            Napier.e(tag = "NetworkService::deleteParticipation") { err.stackTraceToString() }
            return Pair(false, getException(err))
        }
    }

    suspend fun validateRegistrationToken(
        registrationToken: String, endpoint: String? = null
    ): Pair<Study?, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::validateRegistrationToken") { "Validating Registration token..." }
            val httpClient = getHttpClient()
            val url = endpoint ?: endpointRepository.endpoint()
            val registrationApi =
                RegistrationApi(baseUrl = url, httpClientEngine = httpClient.engine)
            val registrationResponse =
                registrationApi.getStudyRegistrationInfo(moreRegistrationToken = registrationToken)
                    ?: return Pair(null, NetworkServiceError(0, "Response null"))
            Napier.i(registrationResponse.response.toString(), tag = TAG)
            if (registrationResponse.success) {
                registrationResponse.body().let {
                    Napier.i(tag = "NetworkService::validateRegistrationToken") { "Registration token valid!" }
                    return Pair(it, null)
                }
            }
            val error = createErrorBody(
                registrationResponse.response.status.value, registrationResponse.response
            )
            return Pair(
                null, error
            )

        } catch (err: Exception) {
            Napier.e(tag = "NetworkService::validateRegistrationToken") { err.stackTraceToString() }
            return Pair(null, getException(err))
        }
    }

    suspend fun sendConsent(
        registrationToken: String, studyConsent: StudyConsent, endpoint: String? = null
    ): Pair<AppConfiguration?, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::sendConsent") { "Sending Consent..." }
            val httpClient = getHttpClient()
            val url = endpoint ?: endpointRepository.endpoint()
            val registrationApi = RegistrationApi(baseUrl = url,
                httpClient.engine,
                httpClientConfig = { httpClient.engineConfig })
            val consentResponse =
                registrationApi.registerForStudy(registrationToken, studyConsent) ?: return Pair(
                    null, NetworkServiceError(0, "Response null")
                )
            if (consentResponse.success) {
                consentResponse.body().let {
                    Napier.i(tag = "NetworkService::sendConsent") { "Credentials received!" }
                    return Pair(it, null)
                }
            }
            return Pair(
                null,
                createErrorBody(consentResponse.response.status.value, consentResponse.response)
            )
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::sendConsent") { e.stackTraceToString() }
            return Pair(null, getException(e))
        }
    }

    suspend fun getStudyConfig(): Pair<Study?, NetworkServiceError?> {
        initConfigApi()
        try {
            Napier.i(tag = "NetworkService::getStudyConfig") { "Downloading study data..." }
            val configResponse = configurationApi?.getStudyConfiguration() ?: return Pair(
                null, NetworkServiceError(null, "No credentials set!")
            )
            if (configResponse.success) {
                configResponse.body().let {
                    Napier.i(tag = "NetworkService::getStudyConfig") { "Loading study data success!" }
                    return Pair(it, null)
                }
            }

            return Pair(
                null, createErrorBody(configResponse.response.status.value, configResponse.response)
            )
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::getStudyConfig") { e.stackTraceToString() }
            return Pair(null, getException(e))
        }
    }

    suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?> {
        initConfigApi()
        configurationApi?.let {
            try {
                Napier.i(tag = "NetworkService::sendNotificationToken") { "Sending notification token..." }
                val tokenResponse = it.setPushNotificationToken(
                    serviceType = PushNotificationServiceType.FCM,
                    pushNotificationToken = PushNotificationToken(token = token)
                ) ?: return Pair(false, NetworkServiceError(0, "Response null"))
                if (tokenResponse.success) {
                    Napier.i(tag = "NetworkService::sendNotificationToken") { "Uploading notification token success!" }
                    return Pair(true, null)
                }
                return Pair(
                    false, createErrorBody(tokenResponse.status, tokenResponse.response)
                )
            } catch (err: Exception) {
                Napier.e(tag = "NetworkService::sendNotificationToken") { err.stackTraceToString() }
                return Pair(false, getException(err))
            }
        }
        return Pair(false, getException(Exception("No credentials found!")))
    }

    suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?> {
        initDataApi()
        try {
            Napier.i(tag = "NetworkService::sendData") { "Sending bulk ${data.bulkId} with ${data.dataPoints.size} datapoints with first being ${data.dataPoints.first()}..." }
            val dataApiResponse = dataApi?.storeBulk(data) ?: return Pair(
                emptySet(), NetworkServiceError(null, "No credentials set!")
            )
            if (dataApiResponse.success) {
                dataApiResponse.body().let {
                    Napier.i(tag = "NetworkService::sendData") { "Sent data!" }
                    dataApiResponse.response.cancel()
                    return Pair(it.toSet() ?: emptySet(), null)
                }
            }
            dataApiResponse.response.cancel()

            return Pair(
                emptySet(), createErrorBody(
                    dataApiResponse.response.status.value, dataApiResponse.response
                )
            )
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::sendData") { e.stackTraceToString() }
            return Pair(emptySet(), getException(e))
        }
    }

    fun iosSendData(
        data: DataBulk,
        completionHandler: (Pair<Set<String>, NetworkServiceError?>) -> Unit
    ) {
        StudyScope.launch {
            completionHandler(
                sendData(data)
            )
        }
    }

    suspend fun downloadMissedNotifications(): List<PushNotification> {
        initNotificationApi()
        val list = notificationApi?.let { notificationApi ->
            try {
                Napier.d(tag = "NetworkService::downloadMissedNotifications") { "Downloading missed notifications from the Server..." }
                notificationApi.listPushNotifications()?.let { response ->
                    if (response.success) {
                        response.body()
                    } else {
                        Napier.d(tag = "NetworkService::downloadMissedNotifications") { "No notifications received from the server" }
                        emptyList()
                    }
                } ?: kotlin.run {
                    Napier.d(tag = "NetworkService::downloadMissedNotifications") { "Notification Response Null" }
                    emptyList()
                }
            } catch (e: Exception) {
                Napier.e(tag = "NetworkService::downloadMissedNotifications") { "Notification List error: $e" }
                return emptyList()
            }
        } ?: emptyList()
        Napier.d(tag = "NetworkService::downloadMissedNotifications") { "Downloaded Messages list: $list" }
        return list
    }

    suspend fun deletePushNotification(msgId: String) {
        initNotificationApi()
        notificationApi?.let { notificationApi ->
            try {
                notificationApi.deleteNotification(msgId)?.let { httpResponse ->
                    if (httpResponse.success) {
                        Napier.d(tag = "NetworkService::deletePushNotification") { "Successfully deleted notification with id: $msgId" }
                    } else {
                        Napier.d(tag = "NetworkService::deletePushNotification") { "Push notification not found with msgID: $msgId. Could not delete!" }
                    }
                }
            } catch (e: Exception) {
                Napier.e(tag = "NetworkService::deletePushNotification") { "Notification deletion error: $e" }
            }
        }
    }

    private fun createErrorBody(code: Int, responseBody: HttpResponse?): NetworkServiceError {
        return try {
            if (responseBody == null) {
                return NetworkServiceError(code = code, message = "Error")
            }
            val error = Json.decodeFromString<Error>(
                responseBody.toString()
            )
            NetworkServiceError(code = code, message = error.msg ?: "Error")
        } catch (e: Exception) {
            getException(e)
        }
    }

    private fun getException(exception: Exception): NetworkServiceError {
        val errorResponse = when (exception) {
            else -> "System error!"
        }
        Napier.e("Exception: ${exception.stackTraceToString()}", tag = TAG)
        exception.printStackTrace()
        return NetworkServiceError(null, errorResponse)
    }

    override fun close() {
        Napier.d(tag = "NetworkService::close") { "Clearing the Http engine..." }
        engineUseCounter = 10
        configurationApi = null
        dataApi = null
        httpClient?.close()
        httpClient = null
    }

    private fun serializeToNDJson(logs: List<Log>): String {
        return buildString {
            logs.forEach { log ->
                appendLine("{\"index\":{}}")
                appendLine(Json.encodeToString(log))
            }
        }
    }
}

