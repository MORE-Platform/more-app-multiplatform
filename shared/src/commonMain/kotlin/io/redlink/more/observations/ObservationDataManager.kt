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
package io.redlink.more.observations

import dev.tmapps.konnection.Konnection
import io.github.aakira.napier.Napier
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.models.StudyState
import io.redlink.more.scopes.AppDispatchers
import io.redlink.more.scopes.MoreDispatchers
import io.redlink.more.scopes.MoreScope
import io.redlink.more.scopes.Scope
import io.redlink.more.scopes.StudyMoreScope
import io.redlink.more.scopes.StudyScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

abstract class ObservationDataManager(
    private val repository: MainRepository,
    private val scope: MoreScope = Scope,
    private val studyScope: StudyMoreScope = StudyScope,
    private val dispatchers: MoreDispatchers = AppDispatchers
) {
    private var countJob: Job? = null

    private var scheduleCount = mutableMapOf<String, Long>()
    protected open val konnection: Konnection? by lazy { Konnection.instance }

    init {
        Napier.i(tag = "ObservationDataManager::init") { "ObservationDataManager init!" }
    }

    open fun add(dataList: List<ObservationDataEntity>, scheduleIdList: Set<String>) {
        if (dataList.isNotEmpty()) {
            Napier.i(tag = "ObservationDataManager::add") { "Adding ${dataList.size} observations for schedule IDs: $scheduleIdList" }
            repository.observationData.addData(dataList)
            repository.dataPointCount.incrementCount(scheduleIdList, dataList.size.toLong())
            if (countJob == null && repository.study.studyState.value == StudyState.ACTIVE) {
                listenToDatapointCountChanges()
            }
        }
    }

    open fun saveAndSend() {
        Napier.i(tag = "ObservationDataManager::saveAndSend") { "Saving and sending observations" }
        scope.launch(dispatchers.io) {
            repository.observationData.store()
        }
    }

    open fun store() {
        Napier.i(tag = "ObservationDataManager::store") { "Storing observations" }
        studyScope.launch(dispatchers.io) {
            repository.observationData.store()
        }
    }

    open fun removeDataPointCount(scheduleId: String) {
        Napier.d(tag = "ObservationDataManager::removeDataPointCount") { "Removing datapoint count for schedule ID: $scheduleId" }
        scheduleCount.remove(scheduleId)
    }

    abstract fun sendData(immediately: Boolean = false, onCompletion: (Boolean) -> Unit = {})

    open suspend fun sendData(immediately: Boolean): Boolean {
        return suspendCancellableCoroutine { cont ->
            sendData(immediately) { success ->
                if (cont.isActive) cont.resume(success)
            }
        }
    }

    open fun listenToDatapointCountChanges() {
        if (countJob == null) {
            Napier.d(tag = "ObservationDataManager::listenToDatapointCountChanges") { "Starting to listen for changes in datapoint counts" }
            countJob = scope.repeatedLaunch(60000, dispatchers.io) {
                if (isConnected()) {
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

    open fun stopListeningToCountChanges() {
        Napier.d(tag = "ObservationDataManager::stopListeningToCAndroiduntChanges") { "Stopped listening for changes in datapoint counts" }
        countJob?.cancel()
        countJob = null
    }

    protected open fun isConnected() = konnection?.isConnected() ?: false

    protected suspend fun dataBulk() = repository.observationData.allAsBulk()

    protected suspend fun deleteAll(idSet: Set<String>) {
        repository.observationData.deleteAllWithId(idSet)
    }
}
