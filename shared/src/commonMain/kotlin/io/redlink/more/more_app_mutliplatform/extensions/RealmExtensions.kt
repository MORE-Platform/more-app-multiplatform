package io.redlink.more.more_app_mutliplatform.extensions

import io.github.aakira.napier.Napier
import io.realm.kotlin.internal.platform.freeze
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.BaseRealmObject
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform

fun <T: BaseRealmObject> RealmQuery<T>.asMappedFlow(): Flow<List<T>> {
    return asFlow().transform { emit(it.list) }
}

fun <T: BaseRealmObject> RealmQuery<T>.firstAsFlow(): Flow<T?> {
    return first().asFlow().transform { emit(it.obj) }
}