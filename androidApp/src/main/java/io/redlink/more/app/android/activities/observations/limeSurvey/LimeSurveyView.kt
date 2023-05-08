package io.redlink.more.app.android.activities.observations.limeSurvey

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.activities.NavigationScreen

@Composable
fun LimeSurveyView(navController: NavController, viewModel: LimeSurveyViewModel) {
    val context = LocalContext.current
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.BLUETOOTH_CONNECTION.route)
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
    viewModel.limeSurveyLink.value?.let { limeSurveyLink ->
        AndroidView(factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                //settings.javaScriptEnabled = true
                loadUrl(limeSurveyLink)
            }
        }, update = {
            Napier.d { "Webview update" }
            it.loadUrl(limeSurveyLink)
        }, modifier = Modifier.fillMaxSize())
    } ?: run {
        Text(text = "System error! Could not load limesurvey data!")
    }
}