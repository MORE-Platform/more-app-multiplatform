package io.redlink.more.app.android.activities.observations.limeSurvey

import android.webkit.WebView
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acsbendi.requestinspectorwebview.WebViewRequest
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.extensions.extractKeyValuePairs
import io.redlink.more.more_app_mutliplatform.viewModels.limeSurvey.CoreLimeSurveyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LimeSurveyViewModel : ViewModel(), WebClientListener {
    private val coreViewModel = CoreLimeSurveyViewModel(MoreApplication.observationFactory!!)
    val limeSurveyLink = mutableStateOf<String?>(null)
    val dataLoading = mutableStateOf(false)
    val wasAnswered = mutableStateOf(false)

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

    fun setScheduleId(scheduleId: String) {
        coreViewModel.setScheduleId(scheduleId)
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

    override fun onNewRequest(webView: WebView, webViewRequest: WebViewRequest) {
        log { "WebViewClient\$shouldInterceptRequest:\nisForMain: ${webViewRequest.isForMainFrame};\nURL: ${webViewRequest.url};\nMethod: ${webViewRequest.method};\nbody: ${webViewRequest.body};\ncookies: ${webViewRequest.headers}" }
        val bodyElements = webViewRequest.body.extractKeyValuePairs()
        if (bodyElements.isNotEmpty()) {
            log { "Body:" }
            bodyElements.forEach { log { "Key: ${it.key}: ${it.value}" } }
        }
        if (webViewRequest.headers.isNotEmpty()) {
            log { "Headers:" }
            webViewRequest.headers.forEach { log { "Key: ${it.key}: ${it.value}" } }
        }
    }
}