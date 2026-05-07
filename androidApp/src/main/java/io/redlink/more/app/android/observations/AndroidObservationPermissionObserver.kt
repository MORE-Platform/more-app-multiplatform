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
import android.content.Context
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.github.aakira.napier.Napier
import io.redlink.more.SharedRes
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.util.ActivityProvider
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.logging.event
import io.redlink.more.observations.ObservationPermissionObserver
import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.observations.observationTypes.AppUsageObservationType
import io.redlink.more.observations.observationTypes.ObservationType
import io.redlink.more.services.store.PermissionApprovalState
import io.redlink.more.services.store.PermissionRepository
import io.redlink.more.services.store.PermissionType

class AndroidObservationPermissionObserver(
    private val permissionRepository: PermissionRepository,
    private val context: Context = MoreApplication.appContext!!
) : ObservationPermissionObserver {

    override fun permissionState(observationType: ObservationType): PermissionApprovalState {
        if (observationType.observationType == AppUsageObservationType().observationType) {
            return permissionRepository.getPermission(PermissionType.APP_TRACKING)
        }

        return if (PermissionUtils.hasAllPermissions(observationType.sensorPermissions, context)) {
            PermissionApprovalState.GRANTED
        } else {
            PermissionApprovalState.DECLINED
        }
    }

    override fun requestPermission(observationType: ObservationType) {
        Napier.d { "Requesting permissions for $observationType" }
        MoreApplication.shared?.observationFactory?.startRequestingPermissions()
        if (observationType.observationType == AppUsageObservationType().observationType) {
            if (permissionState(observationType) != PermissionApprovalState.NOT_SET) {
                MoreApplication.shared?.observationFactory?.stopRequestingPermissions()
                return
            }
            AlertController.openAlertDialog(
                AlertDialogModel(
                    title = StringDesc.Resource(SharedRes.strings.app_tracking_dialog_title),
                    message = StringDesc.Resource(SharedRes.strings.app_tracking_dialog_message),
                    confirmLabel = StringDesc.Resource(SharedRes.strings.app_tracking_dialog_positive_button),
                    cancelLabel = StringDesc.Resource(SharedRes.strings.app_tracking_dialog_negative_button),
                    onConfirm = {
                        Napier.event(LogEvent.APP_TRACKING_ACCEPTED)
                        MoreApplication.shared?.observationFactory?.stopRequestingPermissions()
                    },
                    onDecline = {
                        Napier.event(LogEvent.APP_TRACKING_DECLINED)
                        MoreApplication.shared?.observationFactory?.stopRequestingPermissions()
                    }
                )
            )
            return
        }

        val activity = (context as? Activity) ?: ActivityProvider.getCurrentActivity()
        if (activity != null) {
            MoreApplication.shared!!.observationFactory.observation(observationType.observationType)
                ?.let { observation ->
                    PermissionUtils.requestPermissions(observation, activity) {
                        MoreApplication.shared?.observationFactory?.stopRequestingPermissions()
                    }
                } ?: run {
                MoreApplication.shared?.observationFactory?.stopRequestingPermissions()
            }
        } else {
            MoreApplication.shared?.observationFactory?.stopRequestingPermissions()
        }
    }
}
