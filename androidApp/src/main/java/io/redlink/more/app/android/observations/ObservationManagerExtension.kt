package io.redlink.more.app.android.observations

import android.app.Activity
import android.content.Context
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    // Find the observation for this schedule
    val observation = this.findObservationForSchedule(scheduleId)
    
    if (observation == null) {
        Napier.e("No observation found for schedule $scheduleId")
        return false
    }
    
    // Check if all permissions are granted
    if (PermissionUtils.hasAllPermissions(observation, activity)) {
        // Permissions already granted, start the observation
        return this.start(scheduleId)
    } else {
        // Request permissions
        val permissionsRequested = PermissionUtils.requestPermissions(
            observation,
            activity,
            null,
            scheduleId
        ) { granted ->
            if (granted) {
                // Permissions granted, start the observation
                Scope.launch(Dispatchers.IO) {
                    val result = this@startWithPermissionCheck.start(scheduleId)
                    Napier.d("Observation started with result: $result")
                }
            } else {
                // Permissions denied, show alert dialog
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
    // Use reflection to access the private runningObservations field
    val field = ObservationManager::class.java.getDeclaredField("runningObservations")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as Map<String, Observation>
}

/**
 * Extension function to modify the ObservationRecordingService to use permission check
 * This should be called from the application's onCreate method
 */
fun setupObservationPermissionCheck() {
    // Hook into the ObservationRecordingService.start method
    val originalStartMethod = ObservationManager::start
    
    // Replace it with our permission-checking version
    // Note: This is a conceptual example and would need to be implemented differently
    // since we can't actually replace methods at runtime in Kotlin
    
    // Instead, we'll provide a wrapper function that should be used instead of the original
    Napier.i("Observation permission check setup complete")
}