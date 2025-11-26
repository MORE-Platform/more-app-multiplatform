package io.redlink.umm.blendedcare.app.android.util

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Utility class to safely store and retrieve the current activity
 * Uses WeakReference to prevent memory leaks
 */
object ActivityProvider {
    private var currentActivityRef: WeakReference<Activity>? = null

    /**
     * Sets the current activity
     * This should be called in the activity's onResume method
     * @param activity The current activity
     */
    fun setCurrentActivity(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    /**
     * Clears the current activity
     * This should be called in the activity's onPause method
     */
    fun clearCurrentActivity() {
        currentActivityRef = null
    }

    /**
     * Gets the current activity
     * @return The current activity, or null if no activity is set or the activity has been garbage collected
     */
    fun getCurrentActivity(): Activity? {
        return currentActivityRef?.get()
    }
}