package io.redlink.more.more_app_mutliplatform.extensions

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import io.redlink.more.more_app_mutliplatform.util.createUUID

fun Collection<ObservationDataSchema>.mapAsBulkData(): DataBulk? {
    val dataPoints = this.map { it.asObservationData() }
    val bulkId = createUUID()
    Napier.d { "Created new databulk with ID: $bulkId; Datapoints: $dataPoints" }
    if (dataPoints.isEmpty()) {
        return null
    }
    return DataBulk(
        bulkId = bulkId,
        dataPoints = dataPoints
    )
}