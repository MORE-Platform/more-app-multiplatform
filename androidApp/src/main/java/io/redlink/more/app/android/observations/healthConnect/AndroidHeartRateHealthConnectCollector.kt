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

class AndroidHeartRateHealthConnectCollector(private val context: Context) :
    HealthConnectCollector {
    override val permissionGroup: String = HEALTH_COLLECTOR_GROUP
    override val dataType: HealthConnectDataType = HealthConnectDataType.HEART_RATE

    override suspend fun permissionState(): PermissionApprovalState =
        AndroidHealthConnectManager.permissionState(
            context,
            AndroidHealthConnectManager.Metric.HEART_RATE
        )

    override suspend fun requestPermission() =
        AndroidHealthConnectManager.requestPermissions(
            context,
            AndroidHealthConnectManager.metrics(dataType)
        )

    override suspend fun collect(from: Instant, to: Instant): List<HealthConnectSample> =
        AndroidHealthConnectManager.readSamples(
            context,
            AndroidHealthConnectManager.Metric.HEART_RATE,
            from,
            to
        )
}
