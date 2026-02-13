package io.redlink.umm.participant.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import io.redlink.umm.participant.database.dao.BluetoothDeviceDao
import io.redlink.umm.participant.database.dao.DataPointDao
import io.redlink.umm.participant.database.dao.NotificationDao
import io.redlink.umm.participant.database.dao.ObservationDao
import io.redlink.umm.participant.database.dao.ObservationDataDao
import io.redlink.umm.participant.database.dao.ScheduleDao
import io.redlink.umm.participant.database.dao.StudyDao
import io.redlink.umm.participant.database.entities.BluetoothDeviceEntity
import io.redlink.umm.participant.database.entities.DataPointEntity
import io.redlink.umm.participant.database.entities.NotificationEntity
import io.redlink.umm.participant.database.entities.ObservationDataEntity
import io.redlink.umm.participant.database.entities.ObservationEntity
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.database.entities.StudyEntity

@Database(
    entities = [
        StudyEntity::class,
        ScheduleEntity::class,
        ObservationEntity::class,
        ObservationDataEntity::class,
        NotificationEntity::class,
        BluetoothDeviceEntity::class,
        DataPointEntity::class
    ],
    version = 2
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun observationDao(): ObservationDao
    abstract fun observationDataDao(): ObservationDataDao
    abstract fun notificationDao(): NotificationDao
    abstract fun bluetoothDeviceDao(): BluetoothDeviceDao
    abstract fun dataPointDao(): DataPointDao
}