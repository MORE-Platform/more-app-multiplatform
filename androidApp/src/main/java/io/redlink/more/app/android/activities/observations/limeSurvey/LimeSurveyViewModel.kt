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
package io.redlink.more.app.android.activities.observations.limeSurvey

import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.AlertController
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.web.WebClientListener
import io.redlink.more.models.AlertDialogModel
import io.redlink.more.viewModels.limeSurvey.CoreLimeSurveyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL

class LimeSurveyViewModel(scheduleId: String?, notificationId: String?, observationId: String?) :
    ViewModel(), WebClientListener {
    val coreViewModel = CoreLimeSurveyViewModel(
        MoreApplication.shared!!.repositories,
        MoreApplication.shared!!.observationFactory,
        scheduleId,
        notificationId,
        observationId
    )
    val wasAnswered = mutableStateOf(false)
    val networkLoading = mutableStateOf(false)
    val alertDialogOpen = mutableStateOf<AlertDialogModel?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AlertController.alertDialogModel.collect {
                withContext(Dispatchers.Main) {
                    alertDialogOpen.value = it
                }
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun onFinish() {
        if (wasAnswered.value) {
            coreViewModel.finish()
        } else {
            coreViewModel.cancel()
        }
    }

    override fun onCleared() {
        super.onCleared()
        coreViewModel.close()
    }

    override fun isLoading(loading: Boolean) {
        this.networkLoading.value = loading
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest) {
        if (request.isForMainFrame && request.isRedirect) {
            val (endPath, parameters) = extractPathAndParameters(request.url.toString())
            if (endPath.lowercase().contains("end.htm") && parameters.keys.contains("savedid")) {
                wasAnswered.value = true
            }
        }
    }

    private fun extractPathAndParameters(url: String): Pair<String, Map<String, String>> {
        val urlObject = URL(url)
        val pathParts = urlObject.path.split("/")
        val lastPathPart = pathParts.last()

        val queryPairs = URI(url).query?.split("&")?.mapNotNull {
            val pair = it.split("=")
            if (pair.size == 2) {
                pair[0] to pair[1]
            } else {
                null
            }
        }?.toMap() ?: mapOf()

        return lastPathPart to queryPairs
    }
}