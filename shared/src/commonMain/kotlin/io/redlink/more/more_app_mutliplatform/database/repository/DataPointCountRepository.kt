package io.redlink.more.more_app_mutliplatform.database.repository

import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

class DataPointCountRepository : Repository<DataPointCountSchema>() {
    override fun count(): Flow<Long> {
        return realmDatabase.count<DataPointCountSchema>()
    }

    fun upsert(dataPointCountSchema: DataPointCountSchema) {
        realmDatabase.realm?.writeBlocking {
            val existingObject: DataPointCountSchema? =
                this.query<DataPointCountSchema>(query = "id == $0", dataPointCountSchema.id)
                    .first().find()
            if (existingObject != null) {
                existingObject.count = dataPointCountSchema.count
            } else {
                this.copyToRealm(dataPointCountSchema)
            }
        }
    }

    fun get(scheduleId: ObjectId): Flow<DataPointCountSchema?> {
        return realmDatabase.queryFirst(query = "scheduleId == $scheduleId")
    }

    fun delete(dataPointCountSchema: DataPointCountSchema) {
        realmDatabase.realm?.writeBlocking {
            val existingObject: DataPointCountSchema? = this.query<DataPointCountSchema>(
                query = "id == $0", dataPointCountSchema.id).first().find()
            existingObject?.let {
                this.delete(it)
            }
        }
    }
}