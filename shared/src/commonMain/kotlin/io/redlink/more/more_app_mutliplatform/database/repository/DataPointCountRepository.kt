package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import kotlinx.coroutines.flow.Flow

class DataPointCountRepository : Repository<DataPointCountSchema>() {
    override fun count(): Flow<Long> {
        return realmDatabase.count<DataPointCountSchema>()
    }

    fun incrementCount(scheduleIdSet: Set<String>, addCount: Long = 1) {
        if (scheduleIdSet.isNotEmpty()) {
            realmDatabase.realm?.writeBlocking {
                val dataPointCounts = this.query<DataPointCountSchema>()
                    .find()
                    .filter { it.scheduleId in scheduleIdSet }
                val dataPointScheduleIds = dataPointCounts.map { it.scheduleId }.toSet()
                val (existing, nonExisting) = scheduleIdSet.partition { it in dataPointScheduleIds}
                existing.map { id ->
                    dataPointCounts.firstOrNull { it.scheduleId == id }?.let {
                        it.count += addCount
                    }
                }
                nonExisting.map { id ->
                    this.copyToRealm(DataPointCountSchema().apply {
                        count = addCount
                        this.scheduleId = id
                    })
                }
            }
        }
    }

    fun get(scheduleId: String): Flow<DataPointCountSchema?> {
        return realmDatabase.queryFirst(query = "scheduleId == $0", queryArgs = arrayOf(scheduleId))
    }

    fun delete(scheduleId: String) {
        realmDatabase.realm?.writeBlocking {
            val existingObject: DataPointCountSchema? = this.query<DataPointCountSchema>(
                query = "scheduleId == $0", scheduleId).first().find()
            existingObject?.let {
                this.delete(it)
            }
        }
    }
}