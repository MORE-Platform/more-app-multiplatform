/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.extensions

import io.github.aakira.napier.Napier
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.services.network.openapi.model.DataBulk
import io.redlink.more.util.createUUID

fun Collection<ObservationDataEntity>.mapAsBulkData(): DataBulk? {
    val dataPoints = this.map { it.asObservationData() }
    val bulkId = createUUID()
    if (dataPoints.isEmpty() || dataPoints.firstOrNull() == null) {
        return null
    }
    Napier.i { "Created new databulk with ID: $bulkId; Datapoints: ${dataPoints.size} with first being: ${dataPoints.first()}" }
    return DataBulk(
        bulkId = bulkId,
        dataPoints = dataPoints
    )
}

fun <T> Collection<T>.isSubsetOf(other: Collection<T>): Boolean = this.all { it in other }

fun Set<String>.areAllNamesIn(items: Set<BluetoothDeviceEntity>): Boolean =
    this.all { name -> items.any { item -> item.deviceName?.contains(name) ?: false } }

fun Set<String>.anyNameIn(items: Set<BluetoothDeviceEntity>): Boolean =
    this.any { name ->
        items.any { item ->
            item.deviceName?.lowercase()?.contains(name.lowercase()) == true
        }
    }

