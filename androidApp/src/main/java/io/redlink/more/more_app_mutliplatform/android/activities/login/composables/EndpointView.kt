package io.redlink.more.more_app_mutliplatform.android

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.activities.login.LoginViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.color
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun EndpointView(
    model: LoginViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    val angle: Float by animateFloatAsState(
        targetValue = if (model.endpointItemOpen.value) 180f else 0f,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearEasing
        )
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)) {
            Text(
                text = getStringResource(id = R.string.more_endpoint_label),
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = color(id = R.color.more_main_color),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            IconButton(onClick = { model.toggleEndpointTextField() },
                modifier = Modifier.weight(0.2f)) {
                Icon(
                    Icons.Rounded.ExpandMore,
                    getStringResource(id = R.string.more_endpoint_rotatable_arrow_description),
                    tint = color(id = R.color.more_main_color),
                    modifier = Modifier
                        .fillMaxSize(1.2f)
                        .rotate(angle)
                )
            }
        }
        if (model.endpointItemOpen.value) {
            EndpointInput(model = model, focusRequester = focusRequester, focusManager = focusManager)
        } else {
            Text(
                text = model.dataEndpoint.value,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ErrorMessage(
            hasError = model.isEndpointError(),
            errorMsg = model.error.value ?: getStringResource(
                id = R.string.more_enpoint_error
            )
        )
    }
}

@Composable
fun EndpointInput(
    model: LoginViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    var dataEndpoint by remember { model.dataEndpoint }
    OutlinedTextField(
        value = dataEndpoint,
        onValueChange = {
            dataEndpoint = it
            model.endpointError.value = null
        },
        textStyle = TextStyle(fontSize = 12.sp),
        trailingIcon = {
            if (model.isEndpointError()) {
                Icon(Icons.Filled.Error, "URL Error", tint = color(id = R.color.more_important))
            }
        },
        placeholder = { Text(text = getStringResource(id = R.string.more_endpoint_input_placeholder)) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            autoCorrect = false,
            keyboardType = KeyboardType.Uri
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        isError = model.isEndpointError(),
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