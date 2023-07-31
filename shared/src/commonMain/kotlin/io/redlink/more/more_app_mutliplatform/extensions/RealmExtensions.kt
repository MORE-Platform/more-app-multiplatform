package io.redlink.more.more_app_mutliplatform.extensions

import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun <T: TypedRealmObject> RealmQuery<T>.asMappedFlow(): Flow<List<T>> {
    return asFlow().transform { emit(it.list) }
}

fun <T: TypedRealmObject> RealmQuery<T>.firstAsFlow(): Flow<T?> {
    return first().asFlow().transform { emit(it.obj) }
}