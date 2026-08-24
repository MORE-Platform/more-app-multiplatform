/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

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
    override val aggregatedObservationData: AggregatedObservationDataRepository =
        AggregatedObservationDataRepositoryImpl(appDatabase)

    override suspend fun deleteAll() {
        study.deleteStudy()
        notification.deleteAll()
        aggregatedObservationData.deleteAll()
    }
}
