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

interface MainRepository {
    val study: StudyRepository
    val observation: ObservationRepository
    val observationData: ObservationDataRepository
    val dataPointCount: DataPointCountRepository
    val schedule: ScheduleRepository

    val notification: NotificationRepository
    val bluetoothDevice: BluetoothDeviceRepository
    val aggregatedObservationData: AggregatedObservationDataRepository

    suspend fun deleteAll()
}