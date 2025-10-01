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
import io.redlink.more.more_app_mutliplatform.database.entities.ObservationDataEntity
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.scopes.Scope
import io.redlink.more.more_app_mutliplatform.scopes.StudyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job

abstract class ObservationDataManager(private val repository: MainRepository) {
    private var countJob: Job? = null

    private var scheduleCount = mutableMapOf<String, Long>()
    private val konnection = Konnection.instance

    init {
        Napier.i(tag = "ObservationDataManager::init") { "ObservationDataManager init!" }
    }

    fun add(dataList: List<ObservationDataEntity>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            Napier.i(tag = "ObservationDataManager::add") { "Adding ${dataList.size} observations for schedule IDs: $scheduleIdList" }
            repository.observationData.addData(dataList)
            repository.dataPointCount.incrementCount(scheduleIdList, dataList.size.toLong())
        }
    }

    fun saveAndSend() {
        Napier.i(tag = "ObservationDataManager::saveAndSend") { "Saving and sending observations" }
        StudyScope.launch(Dispatchers.IO) {
            repository.observationData.store()
        }
    }

    fun store() {
        Napier.i(tag = "ObservationDataManager::store") { "Storing observations" }
        StudyScope.launch(Dispatchers.IO) {
            repository.observationData.store()
        }
    }

    fun removeDataPointCount(scheduleId: String) {
        Napier.d(tag = "ObservationDataManager::removeDataPointCount") { "Removing datapoint count for schedule ID: $scheduleId" }
        scheduleCount.remove(scheduleId)
    }

    abstract fun sendData(onCompletion: (Boolean) -> Unit = {})

    fun listenToDatapointCountChanges() {
        if (countJob == null) {
            Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "Starting to listen for changes in datapoint counts" }
            countJob = Scope.repeatedLaunch(60000, Dispatchers.IO) {
                if (konnection.isConnected()) {
                    val count = repository.observationData.getCount()
                    if (count > 0) {
                        Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "Observation data count: $count! Sending data..." }
                        sendData()
                    }
                } else {
                    Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "No connection" }
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
        Scope.launch(Dispatchers.IO) {
            repository.observationData.deleteAllWithId(idSet)
        }
    }
}
