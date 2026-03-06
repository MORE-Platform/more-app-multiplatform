package io.redlink.more.database.repository

interface MainRepository {
    val study: StudyRepository
    val observation: ObservationRepository
    val observationData: ObservationDataRepository
    val dataPointCount: DataPointCountRepository
    val schedule: ScheduleRepository

    val notification: NotificationRepository
    val bluetoothDevice: BluetoothDeviceRepository

    suspend fun deleteAll()
}