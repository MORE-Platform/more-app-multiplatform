package io.redlink.more.more_app_mutliplatform.extensions

import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.BaseRealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun <T: BaseRealmObject> RealmQuery<T>.asMappedFlow(): Flow<List<T>> {
    return asFlow().transform { emit(it.list) }
}

fun <T: BaseRealmObject> RealmQuery<T>.firstAsFlow(): Flow<T?> {
    return first().asFlow().transform { emit(it.obj) }
}