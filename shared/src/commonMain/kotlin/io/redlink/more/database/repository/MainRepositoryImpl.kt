package io.redlink.more.database.repository

import io.redlink.more.database.AppDatabase

class MainRepositoryImpl(appDatabase: AppDatabase) : MainRepository {
    override val study: StudyRepository = StudyRepositoryImpl(appDatabase)
    override val observation: ObservationRepository = ObservationRepositoryImpl(appDatabase)
    override val observationData: ObservationDataRepository =
        ObservationDataRepositoryImpl(appDatabase)
    override val dataPointCount: DataPointCountRepository =
        DataPointCountRepositoryImpl(appDatabase)
    override val schedule: ScheduleRepository = ScheduleRepositoryImpl(appDatabase)

    override val notification: NotificationRepository = NotificationRepositoryImpl(appDatabase)
    override val bluetoothDevice: BluetoothDeviceRepository =
        BluetoothDeviceRepositoryImpl(appDatabase)

    override suspend fun deleteAll() {
        study.deleteStudy()
        notification.deleteAll()
    }
}
