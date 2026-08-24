/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.repository

import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.services.network.openapi.model.DataBulk

interface ObservationDataRepository {
    fun addData(dataList: List<ObservationDataEntity>)

    suspend fun store()

    suspend fun getCount(): Int

    suspend fun allAsBulk(): DataBulk?

    suspend fun deleteAllWithId(idSet: Set<String>)
}