package io.redlink.more.database.repository

import io.redlink.more.database.AppDatabase

open class MainRepository(appDatabase: AppDatabase) {
    open val study = StudyRepository(appDatabase)
    open val observation = ObservationRepository(appDatabase)
    open val observationData = ObservationDataRepository(appDatabase)
    open val dataPointCount = DataPointCountRepository(appDatabase)
    open val schedule = ScheduleRepository(appDatabase)

    open val notification = NotificationRepository(appDatabase)
    open val bluetoothDevice = BluetoothDeviceRepository(appDatabase)

    suspend fun deleteAll() {
        study.deleteStudy()
        notification.deleteAll()
    }
}