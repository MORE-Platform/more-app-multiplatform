package io.redlink.more.more_app_mutliplatform.observations.limesurvey

import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.parametersOf
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.LimeSurveyType
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.flow.MutableStateFlow

class LimeSurveyObservation : Observation(observationType = LimeSurveyType()) {
    val limeURL = MutableStateFlow<String?>(null)

    override fun start(): Boolean {
        return limeURL.value != null
    }

    override fun stop(onCompletion: () -> Unit) {
        limeURL.set(null)
        onCompletion()
    }

    override fun observerAccessible(): Boolean {
        return true
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        val limeSurveyId = settings[LIMESURVEY_ID]?.toString()?.trim('\"')
        val token = settings[LIMESURVEY_TOKEN]?.toString()?.trim('\"')
        val limeSurveyLink = (settings[LIMESURVEY_URL]?.toString()?.trim('\"')
            ?: "https://lime.platform-test.more.redlink.io").replaceFirst(
            Regex("^(http://|https://)"),
            ""
        )
        if (token != null && limeSurveyId != null) {
            val url = configToLink(limeSurveyLink, limeSurveyId, token)
            Napier.i { "LimeSurvey link: $url" }
            limeURL.set(url)
        }
    }

    fun storeData() {
        storeData(emptyMap<String, String>())
    }

    override fun ableToAutomaticallyStart(): Boolean {
        return false
    }

    private fun configToLink(url: String, surveyId: String, token: String): String {
        return URLBuilder(
            URLProtocol.HTTPS,
            url,
            pathSegments = listOf(surveyId),
            parameters = parametersOf("token", token)
        ).build().toString()
    }

    companion object {
        const val LIMESURVEY_ID = "limeSurveyId"
        const val LIMESURVEY_TOKEN = "token"
        const val LIMESURVEY_URL = "limeUrl"
    }
}