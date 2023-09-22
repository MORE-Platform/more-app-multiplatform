package io.redlink.more.app.android.activities.observations.limeSurvey

import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.viewModels.limeSurvey.CoreLimeSurveyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL

class LimeSurveyViewModel : ViewModel(), WebClientListener {
    private val coreViewModel = CoreLimeSurveyViewModel(MoreApplication.shared!!.observationFactory)
    val limeSurveyLink = mutableStateOf<String?>(null)
    val dataLoading = mutableStateOf(false)
    val wasAnswered = mutableStateOf(false)
    val networkLoading = mutableStateOf(false)

    init {
        viewModelScope.launch {
            coreViewModel.dataLoading.collect {
                withContext(Dispatchers.Main) {
                    dataLoading.value = it
                }
            }
        }
        viewModelScope.launch {
            coreViewModel.limeSurveyLink.collect {
                Napier.d { "LimeSurveyLink collected: $it" }
                withContext(Dispatchers.Main) {
                    limeSurveyLink.value = it
                }
            }
        }
    }

    fun setModel(scheduleId: String? = null, observationId: String? = null) {
        if (!scheduleId.isNullOrBlank()) {
            coreViewModel.setScheduleId(scheduleId)
        } else if (!observationId.isNullOrBlank()) {
            coreViewModel.setObservationId(observationId)
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