package io.redlink.more.app.android.observations

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.more_app_mutliplatform.AlertController
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import io.redlink.more.more_app_mutliplatform.observations.Observation

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

    // Store the permission launcher for each activity
    private val permissionLaunchers =
        mutableMapOf<Activity, androidx.activity.result.ActivityResultLauncher<Array<String>>>()

    /**
     * Initializes the permission launcher for an activity
     * This should be called in the activity's onCreate method
     * @param activity The activity to initialize the permission launcher for
     */
    fun initializePermissionLauncher(activity: androidx.activity.ComponentActivity) {
        val permissionLauncher = activity.registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            pendingCallback?.invoke(allGranted)

            // Clear pending data
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
     * Requests all required permissions
     * @param permissions The permissions to request
     * @param activity The activity to request permissions in
     * @param callback Callback for the permission result
     * @return True if all permissions are already granted, false if permissions need to be requested
     */
    fun requestPermissions(
        permissions: Set<String>,
        activity: Activity,
        callback: ((Boolean) -> Unit)? = null
    ): Boolean {
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

        // Store the pending permissions and callback for later use
        pendingPermissions = permissions
        pendingCallback = callback

        // Get the permission launcher for this activity
        val permissionLauncher = permissionLaunchers[activity]
        if (permissionLauncher != null) {
            // Launch the permission request
            permissionLauncher.launch(permissionsToRequest)
        } else {
            // If no permission launcher is found, show a generic permission dialog
            showGenericPermissionDialog(activity)
            callback?.invoke(false)
        }

        return false
    }

    /**
     * Shows a generic permission dialog
     * @param activity The activity to show the dialog in
     */
    private fun showGenericPermissionDialog(activity: Activity) {
        AlertController.openAlertDialog(
            AlertDialogModel(
            title = activity.getString(R.string.required_permissions_not_granted_title),
            message = activity.getString(R.string.required_permission_not_granted_message),
            positiveTitle = activity.getString(R.string.proceed_to_settings_button),
            negativeTitle = activity.getString(R.string.proceed_without_granting_button),
            onPositive = {
                MoreApplication.openSettings.value = true
                AlertController.closeAlertDialog()
            },
            onNegative = {
                AlertController.closeAlertDialog()
            }
        ))
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

        // Store the pending observation and parameters for later use
        pendingObservation = observation
        pendingObservationId = observationId
        pendingScheduleId = scheduleId
        pendingNotificationId = notificationId
        pendingCallback = callback

        // Get the permission launcher for this activity
        val permissionLauncher = permissionLaunchers[activity]
        if (permissionLauncher != null) {
            // Launch the permission request
            permissionLauncher.launch(permissionsToRequest)
        } else {
            // If no permission launcher is found, fall back to showing the permission dialog
            observation.showPermissionAlertDialog()
            callback?.invoke(false)
        }

        return false
    }
}

/**
 * Extension function for Observation to check and request permissions before starting
 * @param observationId The observation ID
 * @param scheduleId The schedule ID
 * @param notificationId The notification ID
 * @param activity The activity to request permissions in
 * @param onPermissionResult Callback for the permission result
 */
fun Observation.checkAndRequestPermissionsBeforeStart(
    observationId: String,
    scheduleId: String,
    notificationId: String? = null,
    activity: Activity,
    onPermissionResult: (Boolean) -> Unit
) {
    if (PermissionUtils.hasAllPermissions(this, activity)) {
        // Permissions already granted, start the observation
        onPermissionResult(true)
    } else {
        // Request permissions
        PermissionUtils.requestPermissions(
            this,
            activity,
            observationId,
            scheduleId,
            notificationId
        ) { granted ->
            if (granted) {
                onPermissionResult(true)
            } else {
                showPermissionAlertDialog()
                onPermissionResult(false)
            }
        }
    }
}

/**
 * Extension function for Observation to start with permission check
 * @param observationId The observation ID
 * @param scheduleId The schedule ID
 * @param notificationId The notification ID
 * @param activity The activity to request permissions in
 * @return True if the observation was started or permissions were requested, false otherwise
 */
fun Observation.startWithPermissionCheck(
    observationId: String,
    scheduleId: String,
    notificationId: String? = null,
    activity: Activity
): Boolean {
    checkAndRequestPermissionsBeforeStart(
        observationId,
        scheduleId,
        notificationId,
        activity
    ) { granted ->
        if (granted) {
            // Start the observation with the original parameters
            this.start(observationId, scheduleId, notificationId)
        }
    }
    return true
}
