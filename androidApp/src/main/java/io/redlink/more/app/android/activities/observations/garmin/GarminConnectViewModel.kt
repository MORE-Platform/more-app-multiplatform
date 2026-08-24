/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.app.android.activities.observations.garmin

import android.webkit.WebResourceRequest
import android.webkit.WebView
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.web.WebClientListener
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.viewModels.garminConnectOAuth.CoreGarminConnectViewModel
import kotlinx.coroutines.runBlocking

class GarminConnectViewModel : WebClientListener {
    val coreViewModel = CoreGarminConnectViewModel(
        MoreApplication.shared!!.networkService,
        MoreApplication.shared!!.sharedStorageRepository
    )

    private var allowedHost: String? = coreViewModel.garminSSOUrl()?.host

    private val injectedUrls = mutableSetOf<String>()

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest
    ) {
        val url = request.url
        val urlString = url.toString()
        Napier.d(tag = "GarminConnectViewModel::shouldOverrideUrlLoading") {
            "Request to: $urlString"
        }

        if (coreViewModel.checkIfUrlIsCallback(urlString)) {
            coreViewModel.setLoading(false)
            Napier.d(tag = "GarminConnectViewModel::shouldOverrideUrlLoading") {
                "Detected Garmin callback URL, handling in ViewModel"
            }

            val callBackSuccess = runBlocking {
                coreViewModel.sendCallback(urlString)
            }

            if (callBackSuccess) {
                coreViewModel.onSuccess()
                view?.post { view.stopLoading() }
                return
            } else {
                AlertController.openAlertDialog(
                    AlertDialogModel.fromStrings(
                        title = "Garmin Connect",
                        message = "Failed to authenticate with Garmin Connect. Please try again later.",
                        confirmLabel = "Ok",
                        onConfirm = { coreViewModel.closeView() }
                    ))
            }
        }

        val host = url.host
        val baseHost = allowedHost
        if (baseHost != null && host != null && !host.endsWith(baseHost)) {
            Napier.d(tag = "GarminConnectViewModel::shouldOverrideUrlLoading") {
                "Host '$host' not matching allowedHost '$baseHost' -> letting WebView handle it"
            }
            return
        }

        if (injectedUrls.remove(urlString)) {
            Napier.d(tag = "GarminConnectViewModel::shouldOverrideUrlLoading") {
                "URL already injected once -> allow"
            }
            return
        }

        if (request.requestHeaders.keys.any { it.equals("Authorization", ignoreCase = true) }) {
            Napier.d(tag = "GarminConnectViewModel::shouldOverrideUrlLoading") {
                "Request already has Authorization header -> allow"
            }
            return
        }

        val authHeader = coreViewModel.basicAuthHeader(urlString)
        if (authHeader == null) {
            Napier.d(tag = "GarminConnectViewModel::shouldOverrideUrlLoading") {
                "No basic auth header available for $urlString -> allow"
            }
            return
        }

        Napier.d(tag = "GarminConnectViewModel::shouldOverrideUrlLoading") {
            "Injecting Authorization header for $urlString"
        }

        injectedUrls.add(urlString)

        view?.post {
            view.stopLoading()
            view.loadUrl(
                urlString,
                mapOf("Authorization" to authHeader)
            )
        }
    }

    override fun isLoading(loading: Boolean) {
        coreViewModel.loading(loading)
    }
}