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
import io.redlink.more.services.network.openapi.model.ParticipantMilestone

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey
    val participantMilestoneId: Int,
    val milestoneId: Int,
    val name: String,
    val dateTime: Long // epoch seconds, same convention as NotificationEntity.timestamp
) {
    companion object {
        fun toEntity(milestone: ParticipantMilestone): MilestoneEntity =
            MilestoneEntity(
                participantMilestoneId = milestone.participantMilestoneId,
                milestoneId = milestone.milestoneId,
                name = milestone.name,
                dateTime = milestone.dateTime.epochSeconds
            )

        fun toEntityList(milestones: List<ParticipantMilestone>): List<MilestoneEntity> =
            milestones.map { toEntity(it) }
    }
}
