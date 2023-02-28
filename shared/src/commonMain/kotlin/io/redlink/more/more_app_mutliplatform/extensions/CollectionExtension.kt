package io.redlink.more.more_app_mutliplatform.extensions

import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import io.redlink.more.more_app_mutliplatform.util.createUUID

fun Set<ObservationData>.mapAsBulkData(): DataBulk? {
    if (isEmpty()) {
        return null
    }
    return DataBulk(
        bulkId = createUUID(),
        dataPoints = this.toSet().toList()
    )
}