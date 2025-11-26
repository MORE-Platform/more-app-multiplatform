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
package io.redlink.umm.participant.services.bluetooth

import io.redlink.umm.participant.database.entities.BluetoothDeviceEntity

interface BluetoothConnectorObserver {

    fun isConnectingToDevice(bluetoothDevice: BluetoothDeviceEntity)

    fun didConnectToDevice(bluetoothDevice: BluetoothDeviceEntity)

    fun didDisconnectFromDevice(bluetoothDevice: BluetoothDeviceEntity)

    fun didFailToConnectToDevice(bluetoothDevice: BluetoothDeviceEntity)

    fun didDiscoverDevice(device: BluetoothDeviceEntity)

    fun removeDiscoveredDevice(device: BluetoothDeviceEntity)

    fun resetAll()
}