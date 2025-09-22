package io.redlink.more.more_app_mutliplatform.database.entities

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