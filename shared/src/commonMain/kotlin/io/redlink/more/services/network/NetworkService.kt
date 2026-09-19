/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.services.network

import io.ktor.http.Url
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.models.CredentialModel
import io.redlink.more.models.LoginModel
import io.redlink.more.services.network.openapi.model.AppConfiguration
import io.redlink.more.services.network.openapi.model.DataBulk
import io.redlink.more.services.network.openapi.model.PushNotification
import io.redlink.more.services.network.openapi.model.Study
import io.redlink.more.services.network.openapi.model.StudyConsent

interface NetworkService {

    fun baseUrl(): String

    suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?>

    suspend fun validateRegistrationToken(loginModel: LoginModel): Pair<Study?, NetworkServiceError?>

    suspend fun sendConsent(
        loginModel: LoginModel, studyConsent: StudyConsent
    ): Pair<AppConfiguration?, NetworkServiceError?>

    suspend fun getStudyConfig(credentials: CredentialModel? = null): Pair<Study?, NetworkServiceError?>

    suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?>

    suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?>

    suspend fun downloadMissedNotifications(): List<PushNotification>

    fun getBasicAuthHeader(): String?

    fun getGarminSSOUrl(): Url?

    fun garminSSOCallbackUrl(): Url?

    suspend fun garminSSOCallback(code: String, status: String): Boolean

    suspend fun deletePushNotification(msgId: String)
}