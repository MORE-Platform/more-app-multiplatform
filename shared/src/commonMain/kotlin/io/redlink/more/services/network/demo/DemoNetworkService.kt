package io.redlink.more.services.network.demo

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
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class DemoNetworkService : NetworkService {
    override fun baseUrl(): String {
        return "https://demo.more-platform.org"
    }


    override suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?> =
        Pair(true, null)

    override suspend fun validateRegistrationToken(loginModel: LoginModel): Pair<Study?, NetworkServiceError?> {
        if (loginModel.token == "DEMO") {
            return Pair(DemoData.createDemoStudy(), null)
        }
        return Pair(null, NetworkServiceError(404, "Not Found"))
    }

    override suspend fun sendConsent(
        loginModel: LoginModel,
        studyConsent: StudyConsent
    ): Pair<AppConfiguration?, NetworkServiceError?> {
        return Pair(DemoData.getDemoAppConfiguration(baseUrl()), null)
    }

    override suspend fun getStudyConfig(credentials: CredentialModel?): Pair<Study?, NetworkServiceError?> {
        return Pair(DemoData.createDemoStudy(), null)
    }

    override suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?> {
        return Pair(true, null)
    }

    override suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?> {
        delay(50.milliseconds)
        return Pair(data.dataPoints.map { it.observationId }.toSet(), null)
    }

    override suspend fun downloadMissedNotifications(): List<PushNotification> {
        return DemoData.getDemoNotifications()
    }

    override fun getBasicAuthHeader(): String? = null

    override fun getGarminSSOUrl(): Url? = null

    override fun garminSSOCallbackUrl(): Url? = null

    override suspend fun garminSSOCallback(code: String, status: String): Boolean = true

    override suspend fun deletePushNotification(msgId: String) {}
}
