package io.redlink.umm.blendedcare.app.android.observations

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier
import io.redlink.umm.participant.observations.Observation

/**
 * Utility class for handling permissions for observations
 */
object PermissionUtils {
    private var pendingObservation: Observation? = null
    private var pendingObservationId: String? = null
    private var pendingScheduleId: String? = null
    private var pendingNotificationId: String? = null
    private var pendingCallback: ((Boolean) -> Unit)? = null
    private var pendingPermissions: Set<String>? = null

    private val permissionLaunchers =
        mutableMapOf<Activity, ActivityResultLauncher<Array<String>>>()

    /**
     * Initializes the permission launcher for an activity
     * This should be called in the activity's onCreate method
     * @param activity The activity to initialize the permission launcher for
     */
    fun initializePermissionLauncher(activity: ComponentActivity) {
        val permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            pendingCallback?.invoke(allGranted)

            pendingObservation = null
            pendingObservationId = null
            pendingScheduleId = null
            pendingNotificationId = null
            pendingPermissions = null
            pendingCallback = null
        }

        permissionLaunchers[activity] = permissionLauncher
    }

    /**
     * Cleans up the permission launcher for an activity
     * This should be called in the activity's onDestroy method
     * @param activity The activity to clean up the permission launcher for
     */
    fun cleanupPermissionLauncher(activity: Activity) {
        permissionLaunchers.remove(activity)
    }

    /**
     * Checks if all required permissions for the observation are granted
     * @param observation The observation to check permissions for
     * @param context The context to check permissions in
     * @return True if all permissions are granted, false otherwise
     */
    fun hasAllPermissions(observation: Observation, context: Context): Boolean {
        val permissions = observation.observationType.sensorPermissions
        return hasAllPermissions(permissions, context)
    }

    /**
     * Checks if all required permissions are granted
     * @param permissions The permissions to check
     * @param context The context to check permissions in
     * @return True if all permissions are granted, false otherwise
     */
    fun hasAllPermissions(permissions: Set<String>, context: Context): Boolean {
        if (permissions.isEmpty()) {
            return true
        }

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Napier.d("Permission not granted: $permission")
                return false
            }
        }
        return true
    }

    /**
     * Requests all required permissions for the observation
     * @param observation The observation to request permissions for
     * @param activity The activity to request permissions in
     * @return True if all permissions are already granted, false if permissions need to be requested
     */
    fun requestPermissions(
        observation: Observation,
        activity: Activity,
        observationId: String? = null,
        scheduleId: String? = null,
        notificationId: String? = null,
        callback: ((Boolean) -> Unit)? = null
    ): Boolean {
        val permissions = observation.observationType.sensorPermissions.toTypedArray()
        if (permissions.isEmpty()) {
            callback?.invoke(true)
            return true
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            callback?.invoke(true)
            return true
        }

        pendingObservation = observation
        pendingObservationId = observationId
        pendingScheduleId = scheduleId
        pendingNotificationId = notificationId
        pendingCallback = callback

        val permissionLauncher = permissionLaunchers[activity]
        if (permissionLauncher != null) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            observation.showPermissionAlertDialog()
            callback?.invoke(false)
        }

        return false
    }
}

