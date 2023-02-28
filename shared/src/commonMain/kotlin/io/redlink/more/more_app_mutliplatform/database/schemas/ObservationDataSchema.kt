package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ObservationDataSchema : RealmObject {
    @PrimaryKey
    var dataId: ObjectId = ObjectId()
    var observationId: String = ""
    var observationType: String = ""
    var dataValue: String = ""
    var timestamp: RealmInstant? = null
}