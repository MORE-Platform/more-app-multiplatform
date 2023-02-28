package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject

class ScheduleSchema : RealmObject {
    var observationId: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var done: Boolean = false
}