package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class DataPointCountSchema: RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId.invoke()
    var scheduleId: String = ""
    var count: Long = 0
}