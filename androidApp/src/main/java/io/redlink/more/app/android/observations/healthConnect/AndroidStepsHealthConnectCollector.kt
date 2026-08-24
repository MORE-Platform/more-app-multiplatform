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
package io.redlink.more.app.android.observations.healthConnect

import android.content.Context
import io.redlink.more.HEALTH_COLLECTOR_GROUP
import io.redlink.more.observations.healthConnect.HealthConnectCollector
import io.redlink.more.observations.healthConnect.HealthConnectDataType
import io.redlink.more.observations.healthConnect.model.HealthConnectSample
import io.redlink.more.services.store.PermissionApprovalState
import kotlin.time.Instant

class AndroidStepsHealthConnectCollector(private val context: Context) : HealthConnectCollector {
    override val permissionGroup: String = HEALTH_COLLECTOR_GROUP
    override val dataType: HealthConnectDataType = HealthConnectDataType.STEPS

    override suspend fun permissionState(): PermissionApprovalState =
        AndroidHealthConnectManager.permissionState(
            context,
            AndroidHealthConnectManager.Metric.STEPS
        )

    override suspend fun requestPermission() =
        // Requested together so distance (a "bonus" field on the daily aggregate, see
        // collectDistanceInMeters) is covered by the same system prompt as steps - distance being
        // denied must not block steps collection, so it is not checked in permissionState().
        AndroidHealthConnectManager.requestPermissions(
            context,
            AndroidHealthConnectManager.metrics(dataType)
        )

    override suspend fun collect(from: Instant, to: Instant): List<HealthConnectSample> =
        AndroidHealthConnectManager.readSamples(
            context,
            AndroidHealthConnectManager.Metric.STEPS,
            from,
            to
        )

    override suspend fun collectDistanceInMeters(from: Instant, to: Instant): Double? =
        AndroidHealthConnectManager.readTotalDistance(context, from, to)

    override suspend fun hasUnrequestedBonusPermission(): Boolean =
        AndroidHealthConnectManager.permissionState(
            context,
            AndroidHealthConnectManager.Metric.DISTANCE
        ) == PermissionApprovalState.NOT_SET
}
