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

import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.services.network.openapi.model.DataBulk

interface ObservationDataRepository {
    fun addData(dataList: List<ObservationDataEntity>)

    /**
     * Like [addData], but bypasses the in-memory queue and suspends until the rows are in the DB.
     * Used to drain a large offline recording chunk-by-chunk without holding it all in memory.
     */
    suspend fun addDataDirectly(dataList: List<ObservationDataEntity>)

    suspend fun store()

    suspend fun getCount(): Int

    suspend fun allAsBulk(): DataBulk?

    /** Oldest-first bounded batch, used to drain the upload queue without OOM. */
    suspend fun nextBatchAsBulk(limit: Int): DataBulk?

    suspend fun deleteAllWithId(idSet: Set<String>)
}