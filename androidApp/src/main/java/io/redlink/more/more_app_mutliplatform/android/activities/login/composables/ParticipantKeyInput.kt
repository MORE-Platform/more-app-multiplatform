package io.redlink.more.more_app_mutliplatform.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.activities.login.LoginViewModel
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.extensions.color
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource

@Composable
fun ParticipationKeyInput(
    model: LoginViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
) {
    var key by remember { model.participantKey }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = getStringResource(id = R.string.more_registration_token_label),
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            color = color(id = R.color.more_main_color)
        )
        Column(verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {

            OutlinedTextField(
                value = key,
                onValueChange = {
                    key = it
                    model.error.value = null
                },
                trailingIcon = {
                    if (model.isTokenError()) {
                        Icon(Icons.Filled.Error, "Error", tint = color(id = R.color.more_important))
                    }
                },
                textStyle = TextStyle(fontSize = 16.sp),
                placeholder = {
                    Text(
                        text = getStringResource(id = R.string.more_registration_token_placeholder),
                        fontSize = 16.sp
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                isError = model.isTokenError(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MoreColors.Main,
                    focusedLabelColor = MoreColors.Main,
                    backgroundColor = Color.Transparent,
                    unfocusedLabelColor = MoreColors.Main,
                    errorLabelColor = MoreColors.Important,
                    cursorColor = MoreColors.Main,
                    errorBorderColor = MoreColors.Important,
                    errorCursorColor = MoreColors.Important,
                    errorLeadingIconColor = MoreColors.Important,
                    errorTrailingIconColor = MoreColors.Important,
                    placeholderColor = MoreColors.InactiveText,
                    unfocusedBorderColor = MoreColors.Main,
                    focusedBorderColor = MoreColors.Main
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ErrorMessage(
                hasError = model.isTokenError(),
                errorMsg = model.error.value ?: getStringResource(
                    id = R.string.more_token_error
                )
            )
        }
    }
}