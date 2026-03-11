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

import io.redlink.more.database.AppDatabase

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