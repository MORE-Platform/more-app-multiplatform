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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.redlink.more.app.android.activities.login.LoginViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.app.android.theme.morePrimary
import io.redlink.umm.blendedcare.app.android.R

@Composable
fun ValidationButton(model: LoginViewModel, focusManager: FocusManager) {
    val isLoading by model.isLoading.collectAsStateWithLifecycle()
    if (!isLoading) {
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                model.validateKey()
            },
            enabled = model.participationKeyNotBlank(),
            colors = ButtonDefaults.morePrimary(),
            border = if (model.participationKeyNotBlank())
                BorderStroke(0.dp, MoreColors.Companion.Primary)
            else
                BorderStroke(2.dp, MoreColors.Companion.SecondaryMedium),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(text = getStringResource(id = R.string.more_login_button_label))
        }
    } else {
        CircularProgressIndicator(strokeWidth = 2.dp, color = MoreColors.Companion.Primary)
    }

}