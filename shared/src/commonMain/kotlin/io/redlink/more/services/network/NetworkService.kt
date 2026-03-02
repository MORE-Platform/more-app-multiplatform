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
package io.redlink.more.services.network

import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.models.CredentialModel
import io.redlink.more.models.LoginModel
import io.redlink.more.services.network.openapi.model.AppConfiguration
import io.redlink.more.services.network.openapi.model.DataBulk
import io.redlink.more.services.network.openapi.model.PushNotification
import io.redlink.more.services.network.openapi.model.PushNotificationServiceType
import io.redlink.more.services.network.openapi.model.PushNotificationToken
import io.redlink.more.services.network.openapi.model.Study
import io.redlink.more.services.network.openapi.model.StudyConsent
import io.redlink.more.services.store.CredentialRepository
import io.redlink.more.services.store.EndpointRepository

private const val TAG = "NetworkService"

class NetworkService(
    endpointRepository: EndpointRepository,
    credentialRepository: CredentialRepository,
) {

    private val networkClients = NetworkClients(credentialRepository, endpointRepository)

    fun baseUrl() = networkClients.baseUrl()

    suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::deleteParticipation") { "Deleting Participation..." }
            val client = networkClients.getRegistrationApi() ?: return Pair(
                false,
                NetworkServiceError(null, "Failed to init HTTP client")
            )

            val response = client.unregisterFromStudy()

            Napier.i(response.toString(), tag = TAG)
            if (response.success) {
                Napier.i(tag = "NetworkService::deleteParticipation") { "Participation deleted!" }
                return Pair(true, null)
            }
            Napier.e(tag = "NetworkService::deleteParticipation") { "Error; Code: ${response.status}" }
            val error = createErrorBody(response.status, response.response)
            return Pair(false, error)
        } catch (err: Exception) {
            Napier.e(tag = "NetworkService::deleteParticipation") { err.stackTraceToString() }
            return Pair(false, getException(err))
        }
    }

    suspend fun validateRegistrationToken(loginModel: LoginModel): Pair<Study?, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::validateRegistrationToken") { "Validating Registration token..." }
            val client = networkClients.getRegistrationApi(loginModel.endpoint) ?: return Pair(
                null,
                NetworkServiceError(null, "HTTP client not initialized")
            )

            val response = client.getStudyRegistrationInfo(loginModel.token)

            Napier.i(response.toString(), tag = TAG)
            if (response.success) {
                val study: Study = response.body()
                Napier.i(tag = "NetworkService::validateRegistrationToken") { "Registration token valid!" }
                return Pair(study, null)
            }
            val error = createErrorBody(response.status, response.response)
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

            val client = networkClients.getRegistrationApi(loginModel.endpoint) ?: return Pair(
                null,
                NetworkServiceError(null, "HTTP client not initialized")
            )

            val response = client.registerForStudy(loginModel.token, studyConsent)

            if (response.success) {
                val appConfig: AppConfiguration = response.body()
                Napier.i(tag = "NetworkService::sendConsent") { "Credentials received!" }
                return Pair(appConfig, null)
            }
            return Pair(null, createErrorBody(response.status, response.response))
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::sendConsent") { e.stackTraceToString() }
            return Pair(null, getException(e))
        }
    }

    suspend fun getStudyConfig(credentials: CredentialModel? = null): Pair<Study?, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::getStudyConfig") { "Downloading study data..." }
            val client = networkClients.getConfigApi(credentials) ?: return Pair(
                null,
                NetworkServiceError(null, "Failed to init HTTP client")
            )

            val response = client.getStudyConfiguration()

            if (response.success) {
                val study: Study = response.body()
                Napier.i(tag = "NetworkService::getStudyConfig") { "Loading study data success!" }
                return Pair(study, null)
            }
            return Pair(null, createErrorBody(response.status, response.response))
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::getStudyConfig") { e.stackTraceToString() }
            return Pair(null, getException(e))
        }
    }

    suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::sendNotificationToken") { "Sending notification token..." }
            val client = networkClients.getConfigApi() ?: return Pair(
                false,
                NetworkServiceError(null, "Failed to init HTTP client")
            )
            val pushToken =
                PushNotificationToken(token = token)

            val response =
                client.setPushNotificationToken(
                    PushNotificationServiceType.FCM,
                    pushToken
                )

            if (response.success) {
                Napier.i(tag = "NetworkService::sendNotificationToken") { "Uploading notification token success!" }
                return Pair(true, null)
            }
            return Pair(false, createErrorBody(response.status, response.response))
        } catch (err: Exception) {
            Napier.e(tag = "NetworkService::sendNotificationToken") { err.stackTraceToString() }
            return Pair(false, getException(err))
        }
    }

    suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?> {
        try {
            Napier.i(tag = "NetworkService::sendData") { "Sending bulk ${data.bulkId} with ${data.dataPoints.size} datapoints with first being ${data.dataPoints.first()}..." }
            val client = networkClients.getDataApi() ?: return Pair(
                emptySet(),
                NetworkServiceError(null, "Failed to init HTTP client")
            )

            val response = client.storeBulk(data)

            if (response.success) {
                val result: List<String> = response.body()
                Napier.i(tag = "NetworkService::sendData") { "Sent data!" }
                return Pair(result.toSet(), null)
            }
            return Pair(emptySet(), createErrorBody(response.status, response.response))
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::sendData") { e.stackTraceToString() }
            return Pair(emptySet(), getException(e))
        }
    }

    suspend fun downloadMissedNotifications(): List<PushNotification> {
        return try {
            Napier.d(tag = "NetworkService::downloadMissedNotifications") { "Downloading missed notifications from the Server..." }
            val client = networkClients.getNotificationApi() ?: return emptyList()

            val response = client.listPushNotifications()

            if (response.success) {
                val notifications: List<PushNotification> = response.body()
                Napier.d(tag = "NetworkService::downloadMissedNotifications") { "Downloaded Messages list: $notifications" }
                notifications
            } else {
                Napier.d(tag = "NetworkService::downloadMissedNotifications") { "No notifications received from the server" }
                emptyList()
            }
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::downloadMissedNotifications") { "Notification List error: $e" }
            emptyList()
        }
    }

    fun getBasicAuthHeader(): String? = networkClients.basicAuthHeader()

    fun getGarminSSOUrl(): Url? {
        try {
            return Url("${baseUrl()}/registration/garmin")
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::getGarminSSOUrl") { "Error getting Garmin SSO Url: $e" }
        }
        return null
    }

    fun garminSSOCallbackUrl(): Url? {
        return getGarminSSOUrl()?.let {
            Url("$it/callback")
        }
    }

    suspend fun garminSSOCallback(code: String, status: String): Boolean {
        try {
            Napier.d(tag = "NetworkService::garminSSOCallback") { "Received callback with code: $code and status: $status" }
            val client = networkClients.getGarminRegistrationApi() ?: run {
                Napier.e(tag = "NetworkService::garminSSOCallback") { "Garmin SSO callback failed: No client available" }
                return false
            }
            val response = client.handleGarminCallback(code, status)
            if (response.success) {
                Napier.d(tag = "NetworkService::garminSSOCallback") { "Garmin SSO callback successful" }
            } else {
                Napier.e(tag = "NetworkService::garminSSOCallback") { "Garmin SSO callback failed: ${response.response}" }
            }
            return response.success
        } catch (e: Exception) {
            Napier.e(tag = "NetworkService::garminSSOCallback") { "Garmin SSO callback failed: $e" }
            return false
        }
    }

    suspend fun deletePushNotification(msgId: String) {
        try {
            val client = networkClients.getNotificationApi() ?: return

            val response = client.deleteNotification(msgId)

            if (response.success) {
                Napier.d(tag = "NetworkService::deletePushNotification") { "Successfully deleted notification with id: $msgId" }
            } else {
                Napier.d(tag = "NetworkService::deletePushNotification") { "Push notification not found with msgID: $msgId. Could not delete!" }
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

            NetworkServiceError(code = code, message = error?.message ?: "Error")
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

}