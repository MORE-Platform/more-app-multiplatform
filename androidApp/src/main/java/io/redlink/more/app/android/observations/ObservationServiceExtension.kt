package io.redlink.more.app.android.observations

import android.app.Activity
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.services.ObservationRecordingService

/**
 * Extension function to start observations with permission check
 * @param scheduleIds The schedule IDs to start
 * @param activity The activity to request permissions in
 */
fun startObservationsWithPermissionCheck(
    scheduleIds: Set<String>,
    activity: Activity
) {
    val observations =
        MoreApplication.Companion.shared?.observationFactory?.observations ?: emptySet()

    if (observations.isEmpty()) {
        ObservationRecordingService.Companion.start(scheduleIds)
        return
    }

    val allPermissionsGranted = observations.all { observation ->
        PermissionUtils.hasAllPermissions(observation, activity)
    }

    if (allPermissionsGranted) {
        ObservationRecordingService.Companion.start(scheduleIds)
    } else {
        val observationNeedingPermissions = observations.firstOrNull { observation ->
            !PermissionUtils.hasAllPermissions(observation, activity)
        }

        if (observationNeedingPermissions != null) {
            PermissionUtils.requestPermissions(
                observationNeedingPermissions,
                activity
            ) { granted ->
                if (granted) {
                    startObservationsWithPermissionCheck(scheduleIds, activity)
                } else {
                    observationNeedingPermissions.showPermissionAlertDialog()
                }
            }
        } else {
            ObservationRecordingService.Companion.start(scheduleIds)
        }
    }
}

