package io.redlink.more.more_app_mutliplatform.database.repository

import io.redlink.more.more_app_mutliplatform.database.AppDatabase

class MainRepository(appDatabase: AppDatabase) {
    val study = StudyRepository(appDatabase)
    val observation = ObservationRepository(appDatabase)
    val observationData = ObservationDataRepository(appDatabase)
    val dataPointCount = DataPointCountRepository(appDatabase)
    val schedule = ScheduleRepository(appDatabase)

    val notification = NotificationRepository(appDatabase)
    val bluetoothDevice = BluetoothDeviceRepository(appDatabase)

    suspend fun deleteAll() {
        study.deleteStudy()
        notification.deleteAll()

    }
}