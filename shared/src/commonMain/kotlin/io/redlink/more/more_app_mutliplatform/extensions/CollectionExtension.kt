package io.redlink.more.more_app_mutliplatform.extensions

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import io.redlink.more.more_app_mutliplatform.util.createUUID

fun Collection<ObservationDataSchema>.mapAsBulkData(): DataBulk? {
    if (isEmpty()) {
        return null
    }
    val dataPoints = this.map { it.asObservationData() }.chunked(1000)
    if (dataPoints.isNotEmpty() && dataPoints[0].isNotEmpty()){
        return DataBulk(
            bulkId = createUUID(),
            dataPoints = dataPoints[0]
        )
    }
    return null
}