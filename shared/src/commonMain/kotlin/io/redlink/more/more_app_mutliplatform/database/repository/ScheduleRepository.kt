package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

class ScheduleRepository: Repository<ScheduleSchema>() {
    override fun count(): Flow<Long> = realmDatabase.count<ScheduleSchema>()

    fun allSchedulesWithStatus(done: Boolean = false) = realmDatabase.query<ScheduleSchema>(query = "done == $0", queryArgs = arrayOf(done))

    fun scheduleWithId(id: String) = realmDatabase.queryFirst<ScheduleSchema>(query = "scheduleId == $0", queryArgs = arrayOf(ObjectId(id)))
}