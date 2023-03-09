package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData
import kotlinx.datetime.Clock
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
    var timestamp: RealmInstant? = null

    fun asObservationData(): ObservationData =
        ObservationData(
            dataId = this.dataId.toHexString(),
            observationId = this.observationId,
            observationType = this.observationType,
            dataValue = Json.decodeFromString(dataValue),
            timestamp = this.timestamp?.toInstant() ?: Clock.System.now()
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
    }
}