package io.redlink.more.more_app_mutliplatform.android.activities.consent.composables

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
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
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.R


@Composable
fun ConsentButtons(model: ConsentViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            model.acceptConsent(context = context)
        } else {
            model.permissionsNotGranted.value = true
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
                    model.getNeededPermissions()
                    checkAndRequestLocationPermissions(context, launcher, model)
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

fun checkAndRequestLocationPermissions(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    model: ConsentViewModel,
) {
    val permissions = model.permissions
    val hasBackgroundLocationPermission = permissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    if (hasBackgroundLocationPermission) {
        permissions.remove(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    if (checkPermissions(context, launcher, permissions)) {
        if (hasBackgroundLocationPermission) {
            checkPermissionForBackgroundLocationAccess(context, launcher, model)
        }
        model.acceptConsent(context = context)
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
        .setPositiveButton("Accept") { _, _ ->
            launcher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))

        }
        .setNegativeButton("Decline") { dialog, _ ->
            dialog.dismiss()
            model.decline()
        }
        .create()
        .show()
}