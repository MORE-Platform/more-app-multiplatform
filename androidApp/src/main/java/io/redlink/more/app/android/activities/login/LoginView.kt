package io.redlink.more.app.android.activities.login

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.activities.login.composables.EndpointView
import io.redlink.more.app.android.activities.login.composables.ParticipationKeyInput
import io.redlink.more.app.android.activities.login.composables.QRCodeButton
import io.redlink.more.app.android.extensions.Image
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.R



@Composable
private fun OpenPermActivity() {
    val context = LocalContext.current as? Activity
    context?.let {
//        val intent = Intent(context, PermissionActivity::class.java)
//        it.finish()
//        it.startActivity(intent)
    }
}

@Composable
fun LoginView(model: LoginViewModel) {
    if (model.tokenIsValid()) {
        OpenPermActivity()
    }
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(id = R.drawable.welcome_to_more, contentDescription = getStringResource(id = R.string.more_welcome_title))
            Spacer(Modifier.height(24.dp))

            LoginForm(model = model)
        }
        Box(Modifier.fillMaxHeight(1f))
    }
}

@Composable
fun LoginForm(model: LoginViewModel) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxWidth(0.90f)) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = getStringResource(id = R.string.more_or_text))
            Spacer(modifier = Modifier.height(16.dp))
            QRCodeButton()
            EndpointView(model = model, focusRequester = focusRequester, focusManager = focusManager)
        }
    }
}