package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant

class Schedule : EmbeddedRealmObject {
    var observationId: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var done: Boolean = false
}