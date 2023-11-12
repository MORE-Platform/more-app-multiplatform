/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
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