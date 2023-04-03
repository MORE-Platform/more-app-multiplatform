package io.redlink.more.more_app_mutliplatform.database.repository

import io.ktor.utils.io.core.*
import io.realm.kotlin.types.BaseRealmObject
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import kotlinx.coroutines.flow.Flow

abstract class Repository<T: BaseRealmObject>(protected val realmDatabase: RealmDatabase = DatabaseManager.database): Closeable {
    private var cache: T? = null

    protected fun setCache(item: T?) {
        cache = item
    }

    fun readCache() = cache

    abstract fun count(): Flow<Long>

    fun collectCount(provideNewState: ((Long) -> Unit)): Closeable {
        return count().asClosure(provideNewState)
    }

    override fun close() {
        realmDatabase.close()
    }
}