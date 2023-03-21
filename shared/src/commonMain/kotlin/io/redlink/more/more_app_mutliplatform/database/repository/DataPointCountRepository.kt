package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import kotlinx.coroutines.flow.Flow

class DataPointCountRepository : Repository<DataPointCountSchema>() {
    override fun count(): Flow<Long> {
        return realmDatabase.count<DataPointCountSchema>()
    }

    fun incrementCount(scheduleId: String) {
        realmDatabase.realm?.writeBlocking {
            val existingObject: DataPointCountSchema? =
                this.query<DataPointCountSchema>(query = "scheduleId == $0", scheduleId)
                    .first().find()
            if (existingObject != null) {
                existingObject.count++
            } else {
                this.copyToRealm(DataPointCountSchema().apply {
                    count = 1
                    this.scheduleId = scheduleId
                })
            }
            Napier.i(tag = "DataPointCountRepository") { "Data Point Count incremented: ${existingObject?.count}" }
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