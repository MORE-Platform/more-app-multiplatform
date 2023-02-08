package io.redlink.more.more_app_mutliplatform.android

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.activities.login.LoginViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.color
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource

@Composable
fun ValidationButton(model: LoginViewModel, focusManager: FocusManager) {
    var loadingState by remember { model.loadingState }
    if (!loadingState) {
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                loadingState = true
//                model.validateKey()
            },
            enabled = model.participationKeyNotBlank() && !loadingState,
            colors = ButtonDefaults
                .buttonColors(
                    backgroundColor = color(id = R.color.more_main_color),
                    contentColor = color(id = R.color.more_white),
                    disabledBackgroundColor = Color.Transparent,
                    disabledContentColor = color(id = R.color.more_inactivity_color)
                ),
            border = if (model.participationKeyNotBlank() && !loadingState)
                BorderStroke(0.dp, color(id = R.color.more_main_color))
            else
                BorderStroke(2.dp, color(id = R.color.more_inactivity_color)),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
        ) {
            Text(text = getStringResource(id = R.string.more_login_button_label))
        }
    } else {
        CircularProgressIndicator(strokeWidth = 2.dp, color = color(id = R.color.more_main_color))
    }

}