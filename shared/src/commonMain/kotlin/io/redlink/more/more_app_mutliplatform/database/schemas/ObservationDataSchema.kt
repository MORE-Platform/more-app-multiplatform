package io.redlink.more.more_app_mutliplatform.database.schemas

import io.github.aakira.napier.Napier
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.asString
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.observations.ObservationBulkModel
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mongodb.kbson.BsonTimestamp
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

    fun clone(): ObservationDataSchema {
        val local = this
        return ObservationDataSchema().apply {
            this.observationType = local.observationType
            this.dataValue = local.dataValue
            this.timestamp = local.timestamp
        }
    }

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
                Napier.i { "fromData last-------------"}
                println("data:")
                println(data)
                
                this.dataValue = data.asString() ?: ""
                Napier.i {this.dataValue}
            }
        }

        fun fromData(data: ObservationBulkModel): ObservationDataSchema {
            Napier.i {"fromDat data-----------------------"}
            println(data)
            return fromData(data.data, data.timestamp)
        }

        fun fromData(data: Collection<ObservationBulkModel>): List<ObservationDataSchema> {
            Napier.i {"fromData collection --------------------"}
            print(data)
            return data.map { fromData(it) }
        }

        fun fromData(
            observationIdSet: Set<String>,
            data: Collection<ObservationBulkModel>
        ): List<ObservationDataSchema> {
            Napier.i { "fromData set---------------" }
            println(data)
            val schemas = fromData(data)
            println(schemas)
            println("data----")
            return observationIdSet.flatMap { id ->
                println(id)
                schemas.map {
                    it.clone().apply { this.observationId = id }
                }
            }
        }
    }
}