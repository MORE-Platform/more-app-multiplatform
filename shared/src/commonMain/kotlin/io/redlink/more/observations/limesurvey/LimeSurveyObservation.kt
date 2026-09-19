/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.observations.limesurvey

import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.parametersOf
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.setNullable
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.LimeSurveyType
import kotlinx.coroutines.flow.MutableStateFlow

class LimeSurveyObservation(repos: MainRepository) :
    Observation(repos, observationType = LimeSurveyType()) {
    val limeURL = MutableStateFlow<String?>(null)

    override fun start(): Boolean {
        return limeURL.value != null
    }

    override fun stop(onCompletion: () -> Unit) {
        limeURL.value = null
        onCompletion()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        val limeSurveyId = settings[LIMESURVEY_ID]?.toString()?.trim('\"')
        val token = settings[LIMESURVEY_TOKEN]?.toString()?.trim('\"')
        val limeSurveyLink = (settings[LIMESURVEY_URL]?.toString()?.trim('\"')
            ?: "https://lime.platform-test.umm.redlink.io")
        if (token != null && limeSurveyId != null) {
            val url = configToLink(limeSurveyLink, limeSurveyId, token)
            Napier.i { "LimeSurvey link: $url" }
            limeURL.setNullable(url)
        }
    }

    fun storeData() {
        storeData(emptyMap<String, String>())
    }

    override fun ableToAutomaticallyStart(): Boolean {
        return false
    }

    private fun configToLink(url: String, surveyId: String, token: String): String {
        val protocol = when {
            url.startsWith("http://", ignoreCase = true) -> URLProtocol.HTTP
            url.startsWith("https://", ignoreCase = true) -> URLProtocol.HTTPS
            else -> URLProtocol.HTTPS
        }

        val cleanUrl = url.replaceFirst(Regex("^(http://|https://)", RegexOption.IGNORE_CASE), "")

        return URLBuilder(
            protocol,
            cleanUrl,
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