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

package io.redlink.more.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_points")
data class DataPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scheduleId: String = "",
    val count: Long = 0L,
) {
}