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
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import io.redlink.more.app.android.activities.consent.ConsentViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.R


@Composable
fun ConsentButtons(model: ConsentViewModel) {
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
                model.openNotificationPermissionDeniedAlertDialog()
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


    if (!model.loading.value) {
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
                    .buttonColors(backgroundColor = MoreColors.Primary,
                        contentColor = MoreColors.White
                    ),
                enabled = !model.loading.value,
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
                    .buttonColors(backgroundColor = MoreColors.Important,
                        contentColor = MoreColors.White),
                enabled = !model.loading.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(5.dp)
            ) {
                Text(text = getStringResource(id = R.string.more_permission_button_decline))
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MoreColors.Primary
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
    val permissions = model.permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    permissions.addAll(extraPermissions)

    val hasBackgroundLocationPermission = permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    if (hasBackgroundLocationPermission) {
        permissions.remove(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    if (hasBackgroundLocationPermission) {
        checkPermissionForBackgroundLocationAccess(context, launcher, model)
    } else {
        checkPermissions(context, launcher, permissions)
    }
}

fun checkPermissions(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    permissions: Set<String>,
): Boolean {
    return if (
        !permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        launcher.launch(permissions.toTypedArray())
        false
    } else {
        launcher.launch(permissions.toTypedArray())
        true
    }
}

fun checkPermissionForBackgroundLocationAccess(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    model: ConsentViewModel,
) {
    if (ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    ) return

    AlertDialog.Builder(context)
        .setTitle(R.string.background_location_permission_title)
        .setMessage(R.string.background_location_permission_message)
        .setPositiveButton("Accept") { dialog, _ ->
            checkAndRequestPermissions(context, launcher, model, setOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            dialog.dismiss()
        }
        .setNegativeButton("Decline") { dialog, _ ->
            checkAndRequestPermissions(context, launcher, model)
            dialog.dismiss()
        }
        .create()
        .show()
}