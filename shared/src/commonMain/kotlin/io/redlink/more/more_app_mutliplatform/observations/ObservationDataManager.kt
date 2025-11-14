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
package io.redlink.more.more_app_mutliplatform.observations

import dev.tmapps.konnection.Konnection
import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull

abstract class ObservationDataManager {
    private val observationDataRepository = ObservationDataRepository()
    private val dataPointCountRepository = DataPointCountRepository()
    private var countJob: Job? = null

    private var scheduleCount = mutableMapOf<String, Long>()
    private val konnection = Konnection.instance

    private var uploadJob: Job? = null

    init {
        Napier.i(tag = "ObservationDataManager::init") { "ObservationDataManager init!" }
    }

    fun add(dataList: List<ObservationDataSchema>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            Napier.i(tag = "ObservationDataManager::add") { "Adding ${dataList.size} observations for schedule IDs: $scheduleIdList" }
            observationDataRepository.addData(dataList)
            dataPointCountRepository.incrementCount(scheduleIdList, dataList.size.toLong())
        }
    }

    fun saveAndSend() {
        Napier.i(tag = "ObservationDataManager::saveAndSend") { "Saving and sending observations" }
        observationDataRepository.store()
    }

    fun store() {
        Napier.i(tag = "ObservationDataManager::store") { "Storing observations" }
        observationDataRepository.store()
    }

    fun removeDataPointCount(scheduleId: String) {
        Napier.d(tag = "ObservationDataManager::removeDataPointCount") { "Removing datapoint count for schedule ID: $scheduleId" }
        scheduleCount.remove(scheduleId)
    }

    abstract fun sendData(onCompletion: (Boolean) -> Unit = {})

    fun listenToDatapointCountChanges() {
        if (countJob == null) {
            Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "Starting to listen for changes in datapoint counts" }
            countJob = Scope.repeatedLaunch(10000) {
                if (konnection.isConnected() && (uploadJob == null || uploadJob?.isActive == false)) {
                    observationDataRepository.count().firstOrNull()?.let {
                        if (it > 0) {
                            Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "Observation data count: $it! Sending data..." }
                            uploadJob = Scope.launch(Dispatchers.IO) {
                                sendData()
                            }.second
                            uploadJob?.invokeOnCompletion {
                                uploadJob = null
                            }
                        }
                    }
                } else {
                    Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "No conncetion" }
                    uploadJob?.cancel()
                }
            }.second
            countJob?.invokeOnCompletion {
                countJob = null
            }
        }
    }

    fun stopListeningToCountChanges() {
        Napier.d(tag = "ObservationDataManager::stopListeningToCountChanges") { "Stopped listening for changes in datapoint counts" }
        countJob?.cancel()
        countJob = null
    }

    private fun deleteAll(idSet: Set<String>) {
        observationDataRepository.deleteAllWithId(idSet)
    }
}