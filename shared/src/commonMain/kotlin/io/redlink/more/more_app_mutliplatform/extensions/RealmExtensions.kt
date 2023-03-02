package io.redlink.more.more_app_mutliplatform.extensions

import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform

fun <T: BaseRealmObject> RealmQuery<T>.asMappedFlow(): Flow<List<T>> {
    return this.asFlow().mapNotNull { results ->
        if (results is InitialResults<T>) {
            return@mapNotNull results.list.map { it }
        }
        return@mapNotNull null
    }
}

fun <T: BaseRealmObject> RealmQuery<T>.firstAsFlow(): Flow<T?> {
    return first().asFlow().transform { emit(it.obj) }
}