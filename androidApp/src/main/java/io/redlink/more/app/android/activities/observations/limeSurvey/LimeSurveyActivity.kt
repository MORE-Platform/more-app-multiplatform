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

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.web.WebClient
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.IconInline
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.theme.MoreColors

class LimeSurveyActivity : ComponentActivity() {
    private val viewModel: LimeSurveyViewModel by lazy {
        LimeSurveyViewModel(
            intent.getStringExtra(LIME_SURVEY_ACTIVITY_SCHEDULE_ID),
            intent.getStringExtra(LIME_SURVEY_ACTIVITY_NOTIFICATION_ID),
            intent.getStringExtra(LIME_SURVEY_ACTIVITY_OBSERVATION_ID)
        )
    }
    var webView: WebView? = null
    var webClientListener: WebClient? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel
        onBackPressedDispatcher.addCallback(this) {
            viewModel.onFinish()
            finish()
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
        setContent {
            LimeSurveyView(viewModel = viewModel, webView)
        }
    }

    override fun onStart() {
        super.onStart()
        log { "LimeSurveyActivity started!" }
        viewModel.viewDidAppear()
    }

    override fun onStop() {
        super.onStop()
        log { "LimeSurveyActivity stopped!" }
        viewModel.viewDidDisappear()
    }

    override fun onDestroy() {
        super.onDestroy()
        log { "LimeSurveyActivity destroyed!" }
        webClientListener?.removeListener()
        webView?.destroy()

    }

    companion object {
        const val LIME_SURVEY_ACTIVITY_SCHEDULE_ID = "LIME_SURVEY_ACTIVITY_SCHEDULE_ID"
        const val LIME_SURVEY_ACTIVITY_OBSERVATION_ID = "LIME_SURVEY_ACTIVITY_OBSERVATION_ID"
        const val LIME_SURVEY_ACTIVITY_NOTIFICATION_ID = "LIME_SURVEY_ACTIVITY_NOTIFICATION_ID"
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LimeSurveyView(viewModel: LimeSurveyViewModel, webView: WebView?) {
    val limeSurveyLink by viewModel.coreViewModel.limeSurveyLink.collectAsStateWithLifecycle(null)
    val dataLoading by viewModel.coreViewModel.dataLoading.collectAsStateWithLifecycle(false)
    val context = LocalContext.current
    if (viewModel.wasAnswered.value) {
        viewModel.onFinish()
        (context as? Activity)?.finish()
    }
    MoreBackground(
        navigationTitle = NavigationScreen.LIMESURVEY.stringRes(),
        maxWidth = 1f,
        leftCornerContent = {
            if (viewModel.networkLoading.value) {
                CircularProgressIndicator(color = MoreColors.Primary, strokeWidth = 2.dp)
            }
        },
        rightCornerContent = {
            IconButton(
                onClick = {
                    viewModel.onFinish()
                    (context as? Activity)?.finish()
                },
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                if (viewModel.wasAnswered.value) {
                    IconInline(
                        icon = Icons.Default.Done,
                        contentDescription = getStringResource(id = R.string.more_lime_finish)
                    )
                } else {
                    IconInline(
                        icon = Icons.Default.Close,
                        contentDescription = getStringResource(id = R.string.more_lime_cancel)
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (dataLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    BasicText(text = "${getStringResource(id = R.string.more_lime_data_loading)}...")
                    CircularProgressIndicator()
                }
            } else {
                webView?.let { webView ->
                    limeSurveyLink?.let { limeSurveyLink ->
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AndroidView(factory = {
                                webView.apply {
                                    loadUrl(limeSurveyLink)
                                }
                            }, update = {
                                Napier.d { "Webview update" }
                                it.loadUrl(limeSurveyLink)
                            }, modifier = Modifier.fillMaxSize())
                        }
                    } ?: run {
                        Text(text = getStringResource(id = R.string.more_lime_survey_loading_error))
                    }
                } ?: run {
                    Text(text = getStringResource(id = R.string.more_lime_survey_loading_error))
                }
            }
        }
    }
}