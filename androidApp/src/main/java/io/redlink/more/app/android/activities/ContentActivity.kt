package io.redlink.more.more_app_mutliplatform.android.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentView
import io.redlink.more.more_app_mutliplatform.android.activities.login.LoginView
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground

class ContentActivity: ComponentActivity() {
    private val viewModel = ContentViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContentView(viewModel = viewModel)
        }
    }
}

@Composable
fun ContentView(viewModel: ContentViewModel) {
    if (viewModel.hasCredentials.value) {
        viewModel.openMainActivity(LocalContext.current)
    } else {
        MoreBackground(showBackButton = false) {
            if (viewModel.loginViewScreenNr.value == 0) {
                LoginView(model = viewModel.loginViewModel)
            } else {
                ConsentView(model = viewModel.consentViewModel)
            }
        }
    }
}
