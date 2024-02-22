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
package io.redlink.more.more_app_mutliplatform.extensions

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.util.createUUID

fun Collection<ObservationDataSchema>.mapAsBulkData(): DataBulk? {
    val dataPoints = this.map { it.asObservationData() }
    val bulkId = createUUID()
    if (dataPoints.isEmpty() || dataPoints.firstOrNull() == null) {
        return null
    }
    Napier.i { "Created new databulk with ID: $bulkId; Datapoints: ${dataPoints.size} with first being: ${dataPoints.first()}" }
    return DataBulk(
        bulkId = bulkId,
        dataPoints = dataPoints
    )
}