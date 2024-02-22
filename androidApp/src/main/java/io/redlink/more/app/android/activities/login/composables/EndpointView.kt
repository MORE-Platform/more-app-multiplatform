/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.login.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.login.LoginViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.ui.theme.MoreColors

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
        ), label = ""
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isOpen = !isOpen }
            ) {
                Text(
                    text = getStringResource(id = R.string.more_endpoint_label),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MoreColors.Primary
                )
                IconButton(onClick = { isOpen = !isOpen }) {
                    Icon(
                        Icons.Rounded.ExpandMore,
                        getStringResource(id = R.string.more_endpoint_rotatable_arrow_description),
                        tint = MoreColors.Primary,
                        modifier = Modifier
                            .rotate(angle)
                    )
                }
            }
        }
        if (isOpen) {
            EndpointInput(
                model = model,
                focusRequester = focusRequester,
                focusManager = focusManager
            )
        }
        BasicText(text = model.currentEndpoint(), fontSize = 10.sp)
    }
}

@Composable
fun EndpointInput(
    model: LoginViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    TextField(
        value = model.dataEndpoint.value,
        onValueChange = {
            model.dataEndpoint.value = it
            model.endpointError.value = null
        },
        textStyle = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center),
        trailingIcon = {
            if (model.isEndpointError()) {
                Icon(Icons.Filled.Error, "URL Error", tint = MoreColors.Important)
            }
        },
        placeholder = {
            Text(
                text = getStringResource(id = R.string.more_endpoint_input_placeholder),
                textAlign = TextAlign.Center
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            autoCorrect = false,
            keyboardType = KeyboardType.Uri
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        isError = model.isEndpointError(),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MoreColors.Secondary,
            focusedLabelColor = MoreColors.Secondary,
            backgroundColor = MoreColors.PrimaryLight,
            unfocusedLabelColor = MoreColors.Secondary,
            errorLabelColor = MoreColors.Important,
            cursorColor = MoreColors.Primary,
            errorBorderColor = MoreColors.Important,
            errorCursorColor = MoreColors.Important,
            errorLeadingIconColor = MoreColors.Important,
            errorTrailingIconColor = MoreColors.Important,
            placeholderColor = MoreColors.TextInactive
        ),
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .height(60.dp)
            .padding(0.dp)
    )
}