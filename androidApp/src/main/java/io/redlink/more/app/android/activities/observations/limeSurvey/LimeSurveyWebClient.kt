package io.redlink.more.app.android.activities.observations.limeSurvey

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.github.aakira.napier.log


interface WebClientListener {
    fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest)

    fun isLoading(loading: Boolean)
}


class LimeSurveyWebClient : WebViewClient(){
    private var clientListener: WebClientListener? = null

    fun setListener(webClientListener: WebClientListener) {
        clientListener = webClientListener
    }

    fun removeListener() {
        clientListener = null
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        log { "WebViewClient\$onPageStarted: $url" }
        clientListener?.isLoading(true)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        log { "WebViewClient\$onPageFinished: $url" }
        clientListener?.isLoading(false)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        log { "WebViewClient\$onPageCommitVisible: $url" }
    }


    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        request?.let {
            log { "WebViewClient\$shouldOverrideUrlLoading: url: ${it.url}; headers: ${it.url}; isForMainFrame: ${it.isForMainFrame}; method: ${it.method}; isRedirect: ${it.isRedirect}" }
            clientListener?.shouldOverrideUrlLoading(view, it)
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}


