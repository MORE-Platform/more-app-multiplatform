package io.redlink.more.more_app_mutliplatform.observations.limesurvey

import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.fullPath
import io.ktor.http.parametersOf
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.LimeSurveyType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LimeSurveyObservation : Observation(observationType = LimeSurveyType()) {
    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    val limeURL = MutableStateFlow<String?>(null)

    override fun start(): Boolean {
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
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
            Napier.d { "LimeSurvey link: $url" }
            scope.launch(Dispatchers.Main) {
                limeURL.emit(url)
            }
        }
    }

    override fun needsToRestartAfterAppClosure(): Boolean {
        return true
    }

    private fun configToLink(url: String, surveyId: String, token: String): String {
        return URLBuilder(
            URLProtocol.HTTPS,
            url,
            pathSegments = listOf("index.php", surveyId),
            parameters = parametersOf("token", token)
        ).build().toString()
    }

    companion object {
        const val LIMESURVEY_ID = "limeSurveyId"
        const val LIMESURVEY_TOKEN = "token"
        const val LIMESURVEY_URL = "limeUrl"
    }
}