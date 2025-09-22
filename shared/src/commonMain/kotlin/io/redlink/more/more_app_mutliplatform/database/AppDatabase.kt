package io.redlink.more.more_app_mutliplatform.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import io.redlink.more.more_app_mutliplatform.database.dao.BluetoothDeviceDao
import io.redlink.more.more_app_mutliplatform.database.dao.DataPointDao
import io.redlink.more.more_app_mutliplatform.database.dao.NotificationDao
import io.redlink.more.more_app_mutliplatform.database.dao.ObservationDao
import io.redlink.more.more_app_mutliplatform.database.dao.ObservationDataDao
import io.redlink.more.more_app_mutliplatform.database.dao.ScheduleDao
import io.redlink.more.more_app_mutliplatform.database.dao.StudyDao
import io.redlink.more.more_app_mutliplatform.database.entities.BluetoothDeviceEntity
import io.redlink.more.more_app_mutliplatform.database.entities.DataPointEntity
import io.redlink.more.more_app_mutliplatform.database.entities.NotificationEntity
import io.redlink.more.more_app_mutliplatform.database.entities.ObservationDataEntity
import io.redlink.more.more_app_mutliplatform.database.entities.ObservationEntity
import io.redlink.more.more_app_mutliplatform.database.entities.ScheduleEntity
import io.redlink.more.more_app_mutliplatform.database.entities.StudyEntity

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
    version = 1
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