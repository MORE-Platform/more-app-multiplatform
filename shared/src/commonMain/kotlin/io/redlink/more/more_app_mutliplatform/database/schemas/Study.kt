package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.MutableRealmInt
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

class Study : RealmObject {
    var studyTitle: String = ""
    var participantInfo: String = ""
    var consentInfo: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var observations: RealmList<Observation>? = null
    var version: Long = 0
}