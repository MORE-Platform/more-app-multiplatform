package io.redlink.more.more_app_mutliplatform.extensions

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import io.redlink.more.more_app_mutliplatform.util.createUUID

fun Collection<ObservationDataSchema>.mapAsBulkData(): DataBulk? {
    val dataPoints = this.map { it.asObservationData() }
    val bulkId = createUUID()
    if (dataPoints.isEmpty() || dataPoints.firstOrNull() == null) {
        return null
    }
    Napier.d { "Created new databulk with ID: $bulkId; Datapoints: ${dataPoints.size} with first being: ${dataPoints.first()}" }
    return DataBulk(
        bulkId = bulkId,
        dataPoints = dataPoints
    )
}