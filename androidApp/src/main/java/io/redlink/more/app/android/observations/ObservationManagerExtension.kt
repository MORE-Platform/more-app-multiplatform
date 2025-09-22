package io.redlink.more.app.android.observations

import android.app.Activity
import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.scopes.Scope
import kotlinx.coroutines.Dispatchers

/**
 * Extension function for ObservationManager to start an observation with permission check
 * @param scheduleId The schedule ID
 * @param activity The activity to request permissions in
 * @return True if the observation was started or permissions were requested, false otherwise
 */
suspend fun ObservationManager.startWithPermissionCheck(
    scheduleId: String,
    activity: Activity
): Boolean {
    val observation = this.findObservationForSchedule(scheduleId)

    if (observation == null) {
        Napier.e("No observation found for schedule $scheduleId")
        return false
    }

    if (PermissionUtils.hasAllPermissions(observation, activity)) {
        return this.start(scheduleId)
    } else {
        val permissionsRequested = PermissionUtils.requestPermissions(
            observation,
            activity,
            null,
            scheduleId
        ) { granted ->
            if (granted) {
                Scope.launch(Dispatchers.IO) {
                    val result = this@startWithPermissionCheck.start(scheduleId)
                    Napier.d("Observation started with result: $result")
                }
            } else {
                observation.showPermissionAlertDialog()
            }
        }

        return permissionsRequested
    }
}

/**
 * Helper function to find the observation for a schedule
 * @param scheduleId The schedule ID
 * @return The observation for the schedule, or null if not found
 */
private fun ObservationManager.findObservationForSchedule(scheduleId: String): Observation? {
    return this.getRunningObservations()[scheduleId]
}

/**
 * Extension property to get the running observations
 * @return The map of running observations
 */
private fun ObservationManager.getRunningObservations(): Map<String, Observation> {
    val field = ObservationManager::class.java.getDeclaredField("runningObservations")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as Map<String, Observation>
}

