package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.*
import io.realm.kotlin.types.BaseRealmObject
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import kotlinx.coroutines.flow.Flow

abstract class Repository<T: BaseRealmObject>: Closeable {
    protected val realmDatabase = DatabaseManager.database

    abstract fun count(): Flow<Long>

    override fun close() {
        realmDatabase.close()
    }
}