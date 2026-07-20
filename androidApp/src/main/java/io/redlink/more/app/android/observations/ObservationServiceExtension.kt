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
        MoreApplication.shared?.observationFactory?.observations ?: emptySet()

    if (observations.isEmpty()) {
        ObservationRecordingService.start(scheduleIds)
        return
    }

    val allPermissionsGranted = observations.all { observation ->
        PermissionUtils.hasAllPermissions(observation, activity)
    }

    if (allPermissionsGranted) {
        ObservationRecordingService.start(scheduleIds)
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
                    observationNeedingPermissions.showPermissionAlertDialog(
                        PermissionUtils.getMissingPermissionNames(
                            observationNeedingPermissions,
                            activity
                        )
                    )
                }
            }
        } else {
            ObservationRecordingService.start(scheduleIds)
        }
    }
}

