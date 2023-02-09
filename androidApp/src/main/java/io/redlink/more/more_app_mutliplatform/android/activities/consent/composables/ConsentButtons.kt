package io.redlink.more.more_app_mutliplatform.android.activities.consent.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun ConsentButtons(model: ConsentViewModel) {
    val context = LocalContext.current

    if (!model.loading.value) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    model.decline()
                },
                colors = ButtonDefaults
                    .buttonColors(backgroundColor = MoreColors.Important,
                        contentColor = MoreColors.White),
                enabled = !model.loading.value,
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(text = getStringResource(id = R.string.more_permission_button_decline))
            }
            Button(
                onClick = {
                    model.acceptConsent(context)
                },
                colors = ButtonDefaults
                    .buttonColors(backgroundColor = MoreColors.Main,
                        contentColor = MoreColors.White
                    ),
                enabled = !model.loading.value,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 32.dp)
            ) {
                Text(text = getStringResource(id = R.string.more_permission_button_accept))
            }

        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MoreColors.Main
            )
        }
    }
}