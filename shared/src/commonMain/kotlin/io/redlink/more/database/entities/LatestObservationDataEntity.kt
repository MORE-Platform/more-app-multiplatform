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
import kotlin.time.Clock

/**
 * Keeps exactly one row per schedule holding the most recently recorded data point for that
 * schedule's observation - unlike [ObservationDataEntity] (full history), this exists only for
 * "current value" visualizations (e.g. today list items), analogous to how [GoalDataEntity]
 * resolves its latest value per schedule instance via `scheduleTimestamp`, but without keeping
 * history: every new data point simply replaces the previous row for that scheduleId.
 */
@Entity(tableName = "latest_observation_data")
data class LatestObservationDataEntity(
    @PrimaryKey val scheduleId: String,
    val observationId: String = "",
    val observationType: String = "",
    val dataValue: String = "",
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
