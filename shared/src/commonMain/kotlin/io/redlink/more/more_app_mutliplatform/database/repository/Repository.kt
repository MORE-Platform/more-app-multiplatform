package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.Closeable
import io.realm.kotlin.types.BaseRealmObject
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import kotlinx.coroutines.flow.Flow

abstract class Repository<T: BaseRealmObject> : Closeable {
    private val database = DatabaseManager
    private var cache: T? = null


    init {
        database.open()
    }

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