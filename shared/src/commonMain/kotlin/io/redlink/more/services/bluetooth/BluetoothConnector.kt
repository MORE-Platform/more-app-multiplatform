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
package io.redlink.more.services.bluetooth

import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.entities.BluetoothDeviceEntity

interface BluetoothConnector : BluetoothConnectorObserver, Closeable {

    var observer: MutableSet<BluetoothConnectorObserver>

    val specificBluetoothConnectors: MutableMap<String, BluetoothConnector>

    fun addSpecificBluetoothConnector(key: String, connector: BluetoothConnector)

    fun addObserver(bluetoothConnectorObserver: BluetoothConnectorObserver)

    fun removeObserver(bluetoothConnectorObserver: BluetoothConnectorObserver)

    fun updateObserver(action: (BluetoothConnectorObserver) -> Unit)

    fun scan()

    fun connect(device: BluetoothDeviceEntity): Error?

    fun disconnect(device: BluetoothDeviceEntity)

    fun stopScanning()
    override fun close()
}