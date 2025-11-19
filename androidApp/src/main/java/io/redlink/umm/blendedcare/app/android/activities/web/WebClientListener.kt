package io.redlink.umm.blendedcare.app.android.activities.web

import android.webkit.WebResourceRequest
import android.webkit.WebView

interface WebClientListener {
    fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest)

    fun isLoading(loading: Boolean)
}