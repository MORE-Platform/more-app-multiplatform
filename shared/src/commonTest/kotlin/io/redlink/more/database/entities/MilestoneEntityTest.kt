package io.redlink.more.database.entities

import io.redlink.more.services.network.openapi.model.ParticipantMilestone
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class MilestoneEntityTest {

    @Test
    fun `toEntity maps fields and converts dateTime to epoch seconds`() {
        val milestone = ParticipantMilestone(
            participantMilestoneId = 1,
            milestoneId = 2,
            name = "First Week",
            dateTime = Instant.fromEpochSeconds(1_700_000_000)
        )

        val entity = MilestoneEntity.toEntity(milestone)

        assertEquals(1, entity.participantMilestoneId)
        assertEquals(2, entity.milestoneId)
        assertEquals("First Week", entity.name)
        assertEquals(1_700_000_000, entity.dateTime)
    }

    @Test
    fun `toEntityList maps every element`() {
        val milestones = listOf(
            ParticipantMilestone(1, 1, "A", Instant.fromEpochSeconds(1)),
            ParticipantMilestone(2, 2, "B", Instant.fromEpochSeconds(2))
        )

        val entities = MilestoneEntity.toEntityList(milestones)

        assertEquals(2, entities.size)
        assertEquals(listOf("A", "B"), entities.map { it.name })
    }
}
