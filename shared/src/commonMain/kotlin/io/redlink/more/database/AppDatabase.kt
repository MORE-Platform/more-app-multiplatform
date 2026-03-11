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

package io.redlink.more.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import io.redlink.more.database.dao.BluetoothDeviceDao
import io.redlink.more.database.dao.DataPointDao
import io.redlink.more.database.dao.NotificationDao
import io.redlink.more.database.dao.ObservationDao
import io.redlink.more.database.dao.ObservationDataDao
import io.redlink.more.database.dao.ScheduleDao
import io.redlink.more.database.dao.StudyDao
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.database.entities.DataPointEntity
import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.entities.StudyEntity

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