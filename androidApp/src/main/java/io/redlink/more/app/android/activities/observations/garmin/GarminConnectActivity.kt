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

package io.redlink.more.app.android.activities.observations.garmin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.web.WebClient
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.IconInline
import io.redlink.more.app.android.shared_composables.MessageAlertDialog
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.viewModels.ViewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GarminConnectActivity : ComponentActivity() {
    val viewModel = GarminConnectViewModel()
    var webView: WebView? = null
    var webClientListener: WebClient? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        onBackPressedDispatcher.addCallback(this) {
            viewModel.coreViewModel.viewDidDisappear()
        }

        webView = WebView(this)
        webView?.let { webView ->
            webClientListener = WebClient()
            webClientListener?.let {
                webClientListener?.setListener(viewModel)
                webView.apply {
                    webViewClient = it
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setNetworkAvailable(true)

                    settings.javaScriptEnabled = true
                }
            }
        }

        lifecycleScope.launch {
            val ssoUrl = viewModel.coreViewModel.garminSSOUrl()?.toString()

            if (ssoUrl != null) {
                val authHeader = viewModel.coreViewModel.basicAuthHeader(ssoUrl)

                withContext(Dispatchers.Main) {
                    if (authHeader != null) {
                        webView?.loadUrl(ssoUrl, mapOf("Authorization" to authHeader))
                    } else {
                        webView?.loadUrl(ssoUrl)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ViewManager.showGarminConnectView.collectLatest {
                    Napier.d { "show GarminConnectView: $it" }
                    if (!it) {
                        finish()
                    }
                }
            }
        }
        setContent {
            GarminConnectSSOView(viewModel = viewModel, webView)
        }

        onBackPressedDispatcher.addCallback(this) {
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                viewModel.coreViewModel.closeView()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        log { "GarminConnectActivity started!" }
        viewModel.coreViewModel.viewDidAppear()
    }

    override fun onStop() {
        super.onStop()
        log { "GarminConnectActivity stopped!" }
        viewModel.coreViewModel.viewDidDisappear()
    }

    override fun onDestroy() {
        super.onDestroy()
        log { "GarminConnectActivity destroyed!" }
        webClientListener?.removeListener()
        webView?.destroy()

    }

}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GarminConnectSSOView(viewModel: GarminConnectViewModel, webView: WebView?) {
    val isLoading by viewModel.coreViewModel.isLoading.collectAsStateWithLifecycle()
    MoreBackground(
        navigationTitle = NavigationScreen.GARMIN_CONNECT.stringRes(),
        maxWidth = 1f,
        leftCornerContent = {
            if (isLoading) {
                CircularProgressIndicator(color = MoreColors.Primary, strokeWidth = 2.dp)
            }
        },
        rightCornerContent = {
            IconButton(
                onClick = {
                    viewModel.coreViewModel.closeView()
                },
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                IconInline(
                    icon = Icons.Default.Close,
                    contentDescription = getStringResource(id = R.string.more_cancel)
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (webView == null || viewModel.coreViewModel.garminSSOUrl() == null) {
                MessageAlertDialog(
                    AlertDialogModel.fromStrings(
                        title = "Garmin Connect",
                        message = getStringResource(id = R.string.garmin_connect_unavailable),
                        confirmLabel = "Ok",
                        onConfirm = { viewModel.coreViewModel.closeView() })
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { webView!! },
                        update = {
                            Napier.d { "Webview update" }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
