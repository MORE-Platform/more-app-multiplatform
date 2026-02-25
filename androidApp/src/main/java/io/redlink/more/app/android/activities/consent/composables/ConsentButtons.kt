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
package io.redlink.more.app.android.activities.consent.composables

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.consent.ConsentViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.observations.PermissionUtils
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.umm.blendedcare.app.android.R

@Composable
fun ConsentButtons(model: ConsentViewModel) {
    val isLoading by model.registrationService.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

        val mutablePermissionMap = permissionsMap.toMutableMap()

        notificationPermission?.let {
            if (mutablePermissionMap[it] == false) {
                model.openNotificationPermissionDeniedAlertDialog(context)
                mutablePermissionMap.remove(it)
            }
        }

        val anyPermissionDenied = mutablePermissionMap.values.any { !it }

        if (anyPermissionDenied) {
            model.openPermissionDeniedAlertDialog(context)
        } else {
            model.acceptConsent(context)
        }
    }


    if (!isLoading) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    checkAndRequestPermissions(context, launcher, model)
                },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = MoreColors.Companion.Primary,
                        contentColor = MoreColors.Companion.White
                    ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(5.dp)
            ) {
                Text(text = getStringResource(id = R.string.more_permission_button_accept))
            }

            Button(
                onClick = {
                    model.decline()
                },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = MoreColors.Companion.Important,
                        contentColor = MoreColors.Companion.White
                    ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(5.dp)
            ) {
                Text(text = getStringResource(id = R.string.more_permission_button_decline))
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MoreColors.Companion.Primary
            )
        }
    }
}

fun checkAndRequestPermissions(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    model: ConsentViewModel,
    extraPermissions: Set<String> = emptySet()
) {
    val permissions =
        MoreApplication.Companion.shared!!.observationFactory.studySensorPermissions()
            .toMutableSet()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    permissions.addAll(extraPermissions)

    permissions.addAll(
        MoreApplication.Companion.shared?.observationFactory?.studySensorPermissions()
            ?: emptySet()
    )

    val hasBackgroundLocationPermission =
        permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    if (hasBackgroundLocationPermission) {
        permissions.remove(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    if (hasBackgroundLocationPermission) {
        checkPermissionForBackgroundLocationAccess(context, launcher, model)
    } else {
        checkPermissions(context, launcher, permissions, model)
    }
}

fun checkPermissions(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    permissions: Set<String>,
    model: ConsentViewModel,
): Boolean {
    return if (!PermissionUtils.hasAllPermissions(permissions, context)) {
        launcher.launch(permissions.toTypedArray())
        false
    } else {
        model.acceptConsent(context)
        true
    }
}

fun checkPermissionForBackgroundLocationAccess(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    model: ConsentViewModel,
) {
    if (PermissionUtils.hasAllPermissions(
            setOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            context
        )
    ) return

    AlertDialog.Builder(context)
        .setTitle(R.string.background_location_permission_title)
        .setMessage(R.string.background_location_permission_message)
        .setPositiveButton("Accept") { dialog, _ ->
            checkAndRequestPermissions(
                context,
                launcher,
                model,
                setOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
            dialog.dismiss()
        }
        .setNegativeButton("Decline") { dialog, _ ->
            checkAndRequestPermissions(context, launcher, model)
            dialog.dismiss()
        }
        .create()
        .show()
}
