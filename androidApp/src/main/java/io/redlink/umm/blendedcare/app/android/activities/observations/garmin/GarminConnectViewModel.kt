package io.redlink.umm.blendedcare.app.android.activities.observations.garmin

import android.webkit.WebResourceRequest
import android.webkit.WebView
import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.activities.web.WebClientListener
import io.redlink.umm.participant.AlertController
import io.redlink.umm.participant.models.AlertDialogModel
import io.redlink.umm.participant.viewModels.garminConnectOAuth.CoreGarminConnectViewModel
import kotlinx.coroutines.runBlocking

class GarminConnectViewModel : WebClientListener {
    val coreViewModel = CoreGarminConnectViewModel(
        BlendedCareApplication.Companion.shared!!.networkService,
        BlendedCareApplication.Companion.shared!!.sharedStorageRepository
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
                    AlertDialogModel(
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