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
package io.redlink.more.database.repository

import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.MilestoneEntity
import kotlinx.coroutines.flow.Flow

class MilestoneRepositoryImpl(private val appDatabase: AppDatabase) : MilestoneRepository {

    override fun getAllFlow(): Flow<List<MilestoneEntity>> =
        appDatabase.milestoneDao().getAllFlow()

    override suspend fun storeMilestones(milestones: List<MilestoneEntity>) {
        appDatabase.milestoneDao().insertAll(milestones)
    }

    override suspend fun deleteAll() {
        appDatabase.milestoneDao().deleteAll()
    }
}
