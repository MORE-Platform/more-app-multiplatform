package io.redlink.more.services.network

import io.ktor.http.Url
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.models.CredentialModel
import io.redlink.more.models.LoginModel
import io.redlink.more.services.network.openapi.model.AppConfiguration
import io.redlink.more.services.network.openapi.model.DataBulk
import io.redlink.more.services.network.openapi.model.ParticipantMilestone
import io.redlink.more.services.network.openapi.model.PushNotification
import io.redlink.more.services.network.openapi.model.Study
import io.redlink.more.services.network.openapi.model.StudyConsent
import io.redlink.more.services.store.SharedStorageRepository

class NetworkServiceProxy(
    private val realService: NetworkService,
    private val demoService: NetworkService?,
    private val sharedStorageRepository: SharedStorageRepository,
) : NetworkService {
    private var activeService: NetworkService =
        if (demoService != null && sharedStorageRepository.load(DEMO_MODE_KEY, false)) {
            demoService
        } else {
            realService
        }

    override fun baseUrl(): String {
        return "demo.data.com"
    }

    override suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?> {
        val (success, error) = activeService.deleteParticipation()
        if (success) {
            sharedStorageRepository.remove(DEMO_MODE_KEY)
            activeService = realService
        }
        return success to error
    }

    override suspend fun validateRegistrationToken(loginModel: LoginModel): Pair<Study?, NetworkServiceError?> {
        if (demoService != null && loginModel.token == "DEMO") {
            sharedStorageRepository.store(DEMO_MODE_KEY, true)
            activeService = demoService
        }
        return activeService.validateRegistrationToken(loginModel)
    }

    override suspend fun sendConsent(
        loginModel: LoginModel,
        studyConsent: StudyConsent
    ): Pair<AppConfiguration?, NetworkServiceError?> =
        activeService.sendConsent(loginModel, studyConsent)

    override suspend fun getStudyConfig(credentials: CredentialModel?): Pair<Study?, NetworkServiceError?> =
        activeService.getStudyConfig(credentials)

    override suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?> =
        activeService.sendNotificationToken(token)

    override suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?> =
        activeService.sendData(data)

    override suspend fun downloadMissedNotifications(): List<PushNotification> =
        activeService.downloadMissedNotifications()

    override suspend fun getMilestones(): List<ParticipantMilestone> =
        activeService.getMilestones()

    override fun getBasicAuthHeader(): String? = activeService.getBasicAuthHeader()
    override fun getGarminSSOUrl(): Url? = activeService.getGarminSSOUrl()
    override fun garminSSOCallbackUrl(): Url? = activeService.garminSSOCallbackUrl()
    override suspend fun garminSSOCallback(code: String, status: String): Boolean =
        activeService.garminSSOCallback(code, status)

    override suspend fun deletePushNotification(msgId: String) =
        activeService.deletePushNotification(msgId)

    companion object {
        const val DEMO_MODE_KEY = "demo_mode"
    }
}
