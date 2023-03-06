package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import kotlinx.coroutines.flow.Flow

class ScheduleRepository: Repository<ScheduleSchema>() {
    override fun count(): Flow<Long> = realmDatabase.count<ScheduleSchema>()

    fun listSchedules(): Flow<List<ScheduleSchema>> = realmDatabase.query()
}