package io.redlink.more.more_app_mutliplatform.android.activities.login

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.login.composables.EndpointView
import io.redlink.more.more_app_mutliplatform.android.activities.login.composables.ParticipationKeyInput
import io.redlink.more.more_app_mutliplatform.android.activities.login.composables.ValidationButton
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository


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
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.fillMaxHeight(0.1f))
        Box(modifier = Modifier.fillMaxWidth(0.65f)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = getStringResource(id = R.string.more_welcome_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    color = MoreColors.MainTitle,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                LoginForm(model = model)
            }
        }
        Box(Modifier.fillMaxHeight(1f))
    }
}

@Composable
fun LoginForm(model: LoginViewModel) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        EndpointView(model = model, focusRequester = focusRequester, focusManager = focusManager)
        Spacer(Modifier.height(32.dp))

        ParticipationKeyInput(model = model,
            focusRequester = focusRequester,
            focusManager = focusManager)
    }

    Spacer(modifier = Modifier.height(16.dp))

    ValidationButton(model = model, focusManager = focusManager)
}