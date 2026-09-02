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

package io.redlink.more.database.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import io.redlink.more.database.AppDatabase

class MainRepositoryImpl(private val appDatabase: AppDatabase) : MainRepository {
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
    override val milestone: MilestoneRepository = MilestoneRepositoryImpl(appDatabase)

    override suspend fun deleteAll() {
        aggregatedObservationData.deleteAll()
        notification.deleteAll()
        study.deleteStudy()
        milestone.deleteAll()
    }

    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        appDatabase.useWriterConnection { it.immediateTransaction { block() } }
}
