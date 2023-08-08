package io.redlink.more.app.android.activities.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.login.composables.EndpointView
import io.redlink.more.app.android.activities.login.composables.ParticipationKeyInput
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.AppVersion


@Composable
fun LoginView(model: LoginViewModel) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(0.95f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                id = R.drawable.welcome_to_more,
                contentDescription = getStringResource(id = R.string.more_welcome_title)
            )

            LoginForm(model = model)
            AppVersion()
        }
    }
}

@Composable
fun LoginForm(model: LoginViewModel) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ParticipationKeyInput(
                model = model,
                focusRequester = focusRequester,
                focusManager = focusManager
            )
        }
        EndpointView(
            model = model,
            focusRequester = focusRequester,
            focusManager = focusManager
        )
    }
}