package io.redlink.more.more_app_mutliplatform.android.activities.login.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.*
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
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.login.LoginViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun EndpointView(
    model: LoginViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    var isOpen by remember {
        mutableStateOf(false)
    }
    val angle: Float by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
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
                color = MoreColors.Primary,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            IconButton(onClick = { isOpen = !isOpen },
                modifier = Modifier.weight(0.2f)) {
                Icon(
                    Icons.Rounded.ExpandMore,
                    getStringResource(id = R.string.more_endpoint_rotatable_arrow_description),
                    tint = MoreColors.Primary,
                    modifier = Modifier
                        .fillMaxSize(1.2f)
                        .rotate(angle)
                )
            }
        }
        if (isOpen) {
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
    }
}

@Composable
fun EndpointInput(
    model: LoginViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    OutlinedTextField(
        value = model.dataEndpoint.value,
        onValueChange = {
            model.dataEndpoint.value = it
            model.endpointError.value = null
        },
        textStyle = TextStyle(fontSize = 12.sp),
        trailingIcon = {
            if (model.isEndpointError()) {
                Icon(Icons.Filled.Error, "URL Error", tint = MoreColors.Important)
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
            textColor = MoreColors.Primary,
            focusedLabelColor = MoreColors.Primary,
            backgroundColor = Color.Transparent,
            unfocusedLabelColor = MoreColors.Primary,
            errorLabelColor = MoreColors.Important,
            cursorColor = MoreColors.Primary,
            errorBorderColor = MoreColors.Important,
            errorCursorColor = MoreColors.Important,
            errorLeadingIconColor = MoreColors.Important,
            errorTrailingIconColor = MoreColors.Important,
            placeholderColor = MoreColors.TextInactive,
            unfocusedBorderColor = MoreColors.Primary,
            focusedBorderColor = MoreColors.Primary
        ),
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .height(60.dp)
    )
}