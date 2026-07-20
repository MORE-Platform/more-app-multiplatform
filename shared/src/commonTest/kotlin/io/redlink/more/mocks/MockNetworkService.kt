package io.redlink.more.mocks

import io.ktor.http.Url
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.models.CredentialModel
import io.redlink.more.models.LoginModel
import io.redlink.more.services.network.NetworkService
import io.redlink.more.services.network.openapi.model.AppConfiguration
import io.redlink.more.services.network.openapi.model.DataBulk
import io.redlink.more.services.network.openapi.model.PushNotification
import io.redlink.more.services.network.openapi.model.Study
import io.redlink.more.services.network.openapi.model.StudyConsent

class MockNetworkService : NetworkService {
    var lastSentToken: String? = null
    var missedNotifications = listOf<PushNotification>()

    override fun baseUrl(): String = "http://localhost"

    override suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?> =
        Pair(true, null)

    override suspend fun validateRegistrationToken(loginModel: LoginModel): Pair<Study?, NetworkServiceError?> =
        Pair(null, null)

    override suspend fun sendConsent(
        loginModel: LoginModel,
        studyConsent: StudyConsent
    ): Pair<AppConfiguration?, NetworkServiceError?> = Pair(null, null)

    override suspend fun getStudyConfig(credentials: CredentialModel?): Pair<Study?, NetworkServiceError?> =
        Pair(null, null)

    override suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?> {
        lastSentToken = token
        return Pair(true, null)
    }

    override suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?> =
        Pair(data.dataPoints.map { it.observationId }.toSet(), null)

    override suspend fun downloadMissedNotifications(): List<PushNotification> =
        missedNotifications

    override fun getBasicAuthHeader(): String? = null

    override fun getGarminSSOUrl(): Url? = null

    override fun garminSSOCallbackUrl(): Url? = null

    override suspend fun garminSSOCallback(code: String, status: String): Boolean = true

    override suspend fun deletePushNotification(msgId: String) {}
}
