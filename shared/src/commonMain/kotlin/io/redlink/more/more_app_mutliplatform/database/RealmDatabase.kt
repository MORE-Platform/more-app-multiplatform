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
package io.redlink.more.more_app_mutliplatform.database

import io.github.aakira.napier.Napier
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import io.redlink.more.more_app_mutliplatform.extensions.asMappedFlow
import io.redlink.more.more_app_mutliplatform.extensions.firstAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlin.reflect.KClass

private const val DB_SCHEMA_VERSION: Long = 4

object RealmDatabase {
    var realm: Realm? = null
        private set

    val mutex = Mutex()

    fun open(realmObjects: Set<KClass<out TypedRealmObject>>) {
        if (realm == null) {
            Napier.i { "Init Realm..." }
            val config = RealmConfiguration.Builder(realmObjects)
                .schemaVersion(DB_SCHEMA_VERSION)
                .build()
            this.realm = Realm.open(config)
        }
    }

    fun close() {
        Napier.i { "Closing Realm..." }
        this.realm?.close()
        this.realm = null
    }

    fun store(
        realmObjects: Collection<RealmObject>,
        updatePolicy: UpdatePolicy = UpdatePolicy.ALL,
    ): Int {
        if (realmObjects.isNotEmpty()) {
            var storedObjects = realmObjects.size
            realm?.writeBlocking {
                realmObjects.forEach {
                    try {
                        copyToRealm(it, updatePolicy)
                    } catch (e: Exception) {
                        Napier.i { "Copy to Realm exception: $e" }
                        storedObjects--;
                    }
                }
            }
            return storedObjects
        }
        return 0
    }

    inline fun <reified T : TypedRealmObject> count(): Flow<Long> {
        return realm?.query<T>()?.count()?.asFlow() ?: emptyFlow()
    }

    inline fun <reified T : TypedRealmObject> findByPrimaryKey(key: String): Flow<T?> {
        return realm?.query<T>("_id == $0", key)?.firstAsFlow() ?: emptyFlow()
    }

    inline fun <reified T : TypedRealmObject> query(
        query: String? = null,
        sortBy: String? = null,
        sort: Sort = Sort.ASCENDING,
        distinctBy: String? = null,
        limit: Int = 0,
        vararg queryArgs: Any
    ): Flow<List<T>> = realm?.let { realm ->
        var realmQuery = query?.let { realm.query<T>(query = it.trim(), args = queryArgs) }
            ?: realm.query(args = queryArgs)
        sortBy?.let { realmQuery = realmQuery.sort(it, sort) }
        distinctBy?.let { realmQuery = realmQuery.distinct(it) }
        if (limit > 0) realmQuery = realmQuery.limit(limit)
        return realmQuery.asMappedFlow()
    } ?: emptyFlow()

    inline fun <reified T : TypedRealmObject, R : Any> queryAllWhereFieldInList(
        field: String,
        list: Set<R>
    ): Flow<List<T>> {
        return realm?.query<T>("${field.trim()} IN $0", list)?.asMappedFlow() ?: emptyFlow()
    }

    inline fun <reified T : TypedRealmObject> queryFirst(
        query: String? = null,
        sortBy: String? = null,
        sort: Sort = Sort.ASCENDING,
        distinctBy: String? = null,
        vararg queryArgs: Any
    ): Flow<T?> {
        return query<T>(
            query,
            sortBy,
            sort,
            distinctBy,
            limit = 1,
            *queryArgs
        ).transform { emit(it.firstOrNull()) }
    }

    inline fun <reified T : TypedRealmObject> deleteAllWhereFieldInList(
        field: String,
        list: List<Any>
    ) {
        realm?.writeBlocking {
            list.map { this.query<T>("${field.trim()} == $0", it).find() }.forEach {
                delete(it)
            }
        }
    }

    fun deleteItems(items: Collection<TypedRealmObject>) {
        realm?.writeBlocking {
            items.forEach {
                delete(it)
            }
        }
    }

    suspend fun <T : TypedRealmObject> deleteAlOfSchema(schema: KClass<T>) {
        realm?.write {
            delete(schema)
        }
    }

    fun deleteAll() {
        Napier.i { "Deleting all data from database..." }
        realm?.writeBlocking {
            this.deleteAll()
        }
    }
}



