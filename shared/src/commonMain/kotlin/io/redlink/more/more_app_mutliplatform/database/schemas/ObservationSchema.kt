package io.redlink.more.more_app_mutliplatform.database.schemas

import io.github.aakira.napier.Napier
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Observation
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.mongodb.kbson.ObjectId


class ObservationSchema : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var observationId: String = ""
    var observationType: String = ""
    var observationTitle: String = ""
    var participantInfo: String = ""
    var configuration: String? = null
    var hidden: Boolean? = null
    var scheduleLess: Boolean = false
    var version: Long = 0
    var required: Boolean = false
    var collectionTimestamp: RealmInstant = RealmInstant.now()

    fun configAsMap(): Map<String, Any> = configuration?.let { config ->
        try {
            Json.decodeFromString<JsonObject>(config).toMap()
        } catch (e: Exception) {
            Napier.e { e.stackTraceToString() }
            emptyMap()
        }
    } ?: emptyMap()

    companion object {
        fun toSchema(observation: Observation): ObservationSchema {
            return ObservationSchema().apply {
                observationId = observation.observationId
                observationTitle = observation.observationTitle
                observationType = observation.observationType
                participantInfo = observation.participantInfo
                configuration = observation.configuration.toString()
                hidden = observation.hidden
                scheduleLess = observation.noSchedule
                required = observation.required
                version = observation.version
            }
        }
    }
}