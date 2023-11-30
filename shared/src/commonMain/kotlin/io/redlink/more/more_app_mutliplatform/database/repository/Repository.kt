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
package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.Closeable
import io.realm.kotlin.types.TypedRealmObject
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import kotlinx.coroutines.flow.Flow

abstract class Repository<T: TypedRealmObject> : Closeable {
    private val database = DatabaseManager
    private var cache: T? = null


//    init {
//        database.open()
//    }

    fun realm() = database.database.realm

    fun realmDatabase() = database.database

    fun mutex() = database.database.mutex

    fun readCache() = cache

    abstract fun count(): Flow<Long>

    fun collectCount(provideNewState: ((Long) -> Unit)): Closeable {
        return count().asClosure(provideNewState)
    }

    override fun close() {
     //   database.close()
    }
}