package io.redlink.more.app.android.activities.observations.limeSurvey

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.acsbendi.requestinspectorwebview.RequestInspectorWebViewClient
import com.acsbendi.requestinspectorwebview.WebViewRequest
import io.github.aakira.napier.log


interface WebClientListener {
    fun onNewRequest(webView: WebView, webViewRequest: WebViewRequest)
}


class LimeSurveyWebClient(webView: WebView) : RequestInspectorWebViewClient(webView){
    private var clientListener: WebClientListener? = null

    fun setListener(webClientListener: WebClientListener) {
        clientListener = webClientListener
    }

    fun removeListener() {
        clientListener = null
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        log { "WebViewClient\$onPageFinished: $url" }
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        log { "WebViewClient\$onPageCommitVisible: $url" }
    }

    override fun shouldInterceptRequest(
        view: WebView,
        webViewRequest: WebViewRequest
    ): WebResourceResponse? {
        clientListener?.onNewRequest(view, webViewRequest)
        return null
    }


    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.let {
            log { "WebViewClient\$shouldOverrideUrlLoading: url: ${it.url}; headers: ${it.url}; isForMainFrame: ${it.isForMainFrame}; method: ${it.method}; isRedirect: ${it.isRedirect}" }
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}


