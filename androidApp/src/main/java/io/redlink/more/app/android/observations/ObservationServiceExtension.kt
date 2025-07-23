package io.redlink.more.app.android.observations

import android.app.Activity
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.services.ObservationRecordingService
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Extension function to start observations with permission check
 * @param scheduleIds The schedule IDs to start
 * @param activity The activity to request permissions in
 */
fun startObservationsWithPermissionCheck(
    scheduleIds: Set<String>,
    activity: Activity
) {
    // Get all observations from the factory
    val observations = MoreApplication.shared?.observationFactory?.observations ?: emptySet()

    if (observations.isEmpty()) {
        // No observations found, start the service directly
        ObservationRecordingService.start(scheduleIds)
        return
    }

    // Check if all permissions are granted
    val allPermissionsGranted = observations.all { observation ->
        PermissionUtils.hasAllPermissions(observation, activity)
    }

    if (allPermissionsGranted) {
        // All permissions granted, start the service
        ObservationRecordingService.start(scheduleIds)
    } else {
        // Request permissions for the first observation that needs them
        val observationNeedingPermissions = observations.firstOrNull { observation ->
            !PermissionUtils.hasAllPermissions(observation, activity)
        }

        if (observationNeedingPermissions != null) {
            PermissionUtils.requestPermissions(
                observationNeedingPermissions,
                activity
            ) { granted ->
                if (granted) {
                    // Check the next observation
                    startObservationsWithPermissionCheck(scheduleIds, activity)
                } else {
                    // Permission denied, show alert dialog
                    observationNeedingPermissions.showPermissionAlertDialog()
                }
            }
        } else {
            // No observations need permissions, start the service
            ObservationRecordingService.start(scheduleIds)
        }
    }
}

/**
 * Extension function to pause observations
 * @param scheduleId The schedule ID to pause
 */
fun pauseObservation(scheduleId: String) {
    ObservationRecordingService.pause(scheduleId)
}

/**
 * Extension function to stop observations
 * @param scheduleId The schedule ID to stop
 */
fun stopObservation(scheduleId: String) {
    ObservationRecordingService.stop(scheduleId)
}

/**
 * Extension function to stop all observations
 */
fun stopAllObservations() {
    ObservationRecordingService.stopAll()
}

/**
 * Extension function to restart all observations
 */
fun restartAllObservations() {
    ObservationRecordingService.restartAll()
}
