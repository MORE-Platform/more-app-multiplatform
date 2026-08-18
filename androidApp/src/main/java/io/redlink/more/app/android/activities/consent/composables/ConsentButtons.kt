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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.consent.ConsentViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.observations.PermissionUtils
import io.redlink.more.app.android.theme.MoreColors
import io.redlink.more.logging.event
import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.observations.observationTypes.AppUsageObservationType
import io.redlink.more.observations.healthConnect.HealthConnectObservationType
import io.redlink.more.scopes.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ConsentButtons(model: ConsentViewModel) {
    val isLoading by model.registrationService.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

        val deniedPermissions = mutablePermissionMap.filter { !it.value }.keys
        if (deniedPermissions.isNotEmpty()) {
            model.openPermissionDeniedAlertDialog(
                context,
                deniedPermissions.map { PermissionUtils.getPermissionLabel(context, it) }
            )
        } else {
            requestHealthConnectPermissionsThenAcceptConsent(context, coroutineScope, model)
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
                    Napier.event(LogEvent.BUTTON_PRESS, "Consent approved")
                    checkAndRequestPermissions(context, launcher, model, coroutineScope)
                },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = MoreColors.Primary,
                        contentColor = MoreColors.White
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
                    Napier.event(LogEvent.BUTTON_PRESS, "Consent declined")
                    model.decline()
                },
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = MoreColors.Important,
                        contentColor = MoreColors.White
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
                color = MoreColors.Primary
            )
        }
    }
}

fun checkAndRequestPermissions(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    model: ConsentViewModel,
    coroutineScope: CoroutineScope,
    extraPermissions: Set<String> = emptySet()
) {
    val permissions =
        MoreApplication.shared!!.observationFactory.studySensorPermissions()
            .toMutableSet()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    permissions.addAll(extraPermissions)

    permissions.addAll(
        MoreApplication.shared?.observationFactory?.studySensorPermissions()
            ?: emptySet()
    )

    permissions.removeAll(AppUsageObservationType().sensorPermissions)

    val hasBackgroundLocationPermission =
        permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    if (hasBackgroundLocationPermission) {
        permissions.remove(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    if (hasBackgroundLocationPermission) {
        checkPermissionForBackgroundLocationAccess(context, launcher, model, coroutineScope)
    } else {
        checkPermissions(context, launcher, permissions, model, coroutineScope)
    }
}

fun checkPermissions(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    permissions: Set<String>,
    model: ConsentViewModel,
    coroutineScope: CoroutineScope,
): Boolean {
    return if (!PermissionUtils.hasAllPermissions(permissions, context)) {
        launcher.launch(permissions.toTypedArray())
        false
    } else {
        requestHealthConnectPermissionsThenAcceptConsent(context, coroutineScope, model)
        true
    }
}

fun checkPermissionForBackgroundLocationAccess(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    model: ConsentViewModel,
    coroutineScope: CoroutineScope,
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
                coroutineScope,
                setOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
            dialog.dismiss()
        }
        .setNegativeButton("Decline") { dialog, _ ->
            checkAndRequestPermissions(context, launcher, model, coroutineScope)
            dialog.dismiss()
        }
        .create()
        .show()
}

/**
 * Enqueues a Health Connect permission check for whichever subtypes the study actually needs (if
 * any), by delegating to [HealthConnectObservation]'s own collector-aware permission check - the
 * same logic that already runs at schedule-start time - instead of duplicating it here. This
 * automatically scopes to active collectors, requests only what's missing, and shows the
 * missing-permission alert on decline.
 *
 * The check runs on the app-wide [Scope] rather than [coroutineScope] (which is tied to this
 * composition and dies when ContentActivity is torn down after consent completes), and does not
 * block consent submission - AndroidHealthConnectManager queues the actual system prompt until
 * MainActivity is resumed, so waiting for it here would only stall the consent flow.
 */
fun requestHealthConnectPermissionsThenAcceptConsent(
    context: Context,
    coroutineScope: CoroutineScope,
    model: ConsentViewModel
) {
    Scope.launch {
        MoreApplication.shared?.observationFactory
            ?.observation(HealthConnectObservationType().observationType)
            ?.updateObservationPermissions()
    }
    coroutineScope.launch {
        model.registrationService.beginConsentSubmission()
        model.acceptConsent(context)
    }
}
