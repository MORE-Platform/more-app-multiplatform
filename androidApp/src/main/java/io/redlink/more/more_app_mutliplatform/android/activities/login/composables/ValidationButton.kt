package io.redlink.more.more_app_mutliplatform.android.activities.login.composables

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
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.login.LoginViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.color
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun ValidationButton(model: LoginViewModel, focusManager: FocusManager) {
    if (!model.loadingState.value) {
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                model.validateKey()
            },
            enabled = model.participationKeyNotBlank() && !model.loadingState.value,
            colors = ButtonDefaults
                .buttonColors(
                    backgroundColor = MoreColors.Main,
                    contentColor = MoreColors.White,
                    disabledBackgroundColor = Color.Transparent,
                    disabledContentColor = MoreColors.Inactivity
                ),
            border = if (model.participationKeyNotBlank() && !model.loadingState.value)
                BorderStroke(0.dp, MoreColors.Main)
            else
                BorderStroke(2.dp, MoreColors.Inactivity),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
        ) {
            Text(text = getStringResource(id = R.string.more_login_button_label))
        }
    } else {
        CircularProgressIndicator(strokeWidth = 2.dp, color = MoreColors.Main)
    }

}