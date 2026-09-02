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
package io.redlink.more.database.dao

import androidx.room.Dao
import androidx.room.Query
import io.redlink.more.database.entities.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao : BaseDao<MilestoneEntity> {

    @Query("SELECT * FROM milestones ORDER BY dateTime DESC")
    fun getAllFlow(): Flow<List<MilestoneEntity>>

    @Query("DELETE FROM milestones")
    suspend fun deleteAll()
}
