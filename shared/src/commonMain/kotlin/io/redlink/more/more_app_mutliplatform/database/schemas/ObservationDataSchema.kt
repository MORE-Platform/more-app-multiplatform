package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.asString
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.observations.ObservationBulkModel
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mongodb.kbson.ObjectId

class ObservationDataSchema : RealmObject {
    @PrimaryKey
    var dataId: ObjectId = ObjectId()
    var observationId: String = ""
    var observationType: String = ""
    var dataValue: String = ""
    var timestamp: RealmInstant = RealmInstant.now()

    fun asObservationData(): ObservationData =
        ObservationData(
            dataId = this.dataId.toHexString(),
            observationId = this.observationId,
            observationType = this.observationType,
            dataValue = Json.decodeFromString(dataValue),
            timestamp = this.timestamp.toInstant()
        )

    override fun toString(): String {
        return "dataId: $dataId; observationId: $observationId; observationType: $observationType, timestamp: $timestamp, data: $dataValue;"
    }

    companion object {
        fun fromObservationData(observationData: ObservationData): ObservationDataSchema {
            return ObservationDataSchema().apply {
                observationId = observationData.observationId
                observationType = observationData.observationType
                dataValue = observationData.dataValue?.let { Json.encodeToString(it) } ?: ""
                timestamp = observationData.timestamp.toRealmInstant()
            }
        }

        fun fromData(data: Any, timestamp: Long = -1): ObservationDataSchema {
            return ObservationDataSchema().apply {
                if (timestamp > 0) {
                    this.timestamp =
                        RealmInstant.from(epochSeconds = timestamp, nanosecondAdjustment = 0)
                }
                this.dataValue = data.asString() ?: "{}"
            }
        }

        fun fromData(data: ObservationBulkModel): ObservationDataSchema {
            return fromData(data.data, data.timestamp)
        }

        fun fromData(data: Collection<ObservationBulkModel>): List<ObservationDataSchema> {
            return data.map { fromData(it) }
        }

        fun fromData(
            observationIdSet: Set<String>,
            data: Collection<ObservationBulkModel>
        ): List<ObservationDataSchema> {
            return observationIdSet.flatMap {  id ->
                fromData(data).map { it.apply { observationId = id } }
            }
        }
    }
}