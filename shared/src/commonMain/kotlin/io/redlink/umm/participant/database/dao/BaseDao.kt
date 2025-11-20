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
package io.redlink.umm.participant.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Base DAO interface containing standard CRUD operations that are common across all DAOs.
 * All entity-specific DAO interfaces should extend this interface to inherit standard operations.
 *
 * @param T The entity type this DAO operates on
 */
interface BaseDao<T> {

    /**
     * Insert a single entity into the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T)

    /**
     * Insert a list of entities into the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>)

    /**
     * Update an existing entity in the database
     */
    @Update
    suspend fun update(entity: T)

    @Update
    suspend fun updateAll(entities: List<T>)

    /**
     * Delete a single entity from the database
     */
    @Delete
    suspend fun delete(entity: T)
}