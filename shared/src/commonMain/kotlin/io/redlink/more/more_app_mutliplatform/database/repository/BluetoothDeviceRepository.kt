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
package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BluetoothDeviceRepository(private val bluetoothConnector: BluetoothConnector? = null) :
    Repository<BluetoothDevice>(), BluetoothConnectorObserver {

    private val deviceManager = BluetoothDeviceManager

    init {
        launch {
            getDevices().cancellable().collect {
                deviceManager.addPairedDeviceIds(it.toSet())
            }
        }
    }

    override fun count(): Flow<Long> = realmDatabase().count<BluetoothDevice>()

    fun listenForConnectedDevices() {
        launch {
            getConnectedDevices().cancellable().collect {
                deviceManager.addConnectedDevices(it.toSet())
            }
        }
    }

    fun setConnectionState(bluetoothDevice: BluetoothDevice, connected: Boolean) {
        if (connected) {
            deviceManager.addConnectingDevices(setOf(bluetoothDevice))
        }
        launch {
            realm()?.write {
                bluetoothDevice.address?.let {
                    val device = this.query<BluetoothDevice>("address = $0", it).first().find()
                    if (device != null) {
                        Napier.i { "Setting state for device: $device to $connected" }
                        device.connected = connected
                        device.shouldAutomaticallyReconnect = connected
                    } else {
                        Napier.i { "Adding device $bluetoothDevice and setting state to $connected" }
                        bluetoothDevice.connected = connected
                        bluetoothDevice.shouldAutomaticallyReconnect = connected
                        this.copyToRealm(bluetoothDevice, updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        }
    }

    fun setAllConnectionStates(connected: Boolean) {
        realm()?.writeBlocking {
            this.query<BluetoothDevice>()
                .find()
                .map {
                    it.connected = connected
                }
        }
    }

    fun setAutoReconnect(bluetoothDevice: BluetoothDevice, shouldAutoReconnect: Boolean) {
        launch {
            realm()?.write {
                bluetoothDevice.address?.let {
                    val device = this.query<BluetoothDevice>("address = $0", it).first().find()
                    if (device != null) {
                        Napier.i { "Setting auto reconnect state for device: $device to $shouldAutoReconnect" }
                        device.shouldAutomaticallyReconnect = shouldAutoReconnect
                    }
                }
            }
        }
    }

    fun getAllDevicesWithAutoReconnectEnabled() =
        realmDatabase().query<BluetoothDevice>(query = "shouldAutomaticallyReconnect = true")

    fun setConnectionState(deviceIds: Set<String>, connected: Boolean) {
        realm()?.writeBlocking {
            this.query<BluetoothDevice>()
                .find()
                .filter { it.deviceId in deviceIds }
                .map {
                    it.connected = connected
                }
        }
    }

    fun getDevices() = realmDatabase().query<BluetoothDevice>()

    fun getConnectedDevices(connected: Boolean = true) = realmDatabase().query<BluetoothDevice>(
        query = "connected = $0",
        queryArgs = arrayOf(connected)
    )

    fun connectedDevicesChange(
        connected: Boolean,
        provideNewState: (List<BluetoothDevice>) -> Unit
    ) = getConnectedDevices(connected).asClosure(provideNewState)

    fun removeDevices(deviceIds: Set<BluetoothDevice>) {
        realmDatabase().deleteItems(deviceIds)
    }

    fun updateConnectedDevices(listenForTimeInMillis: Long = 5000) {
        bluetoothConnector?.let { bluetoothConnector ->
            CoroutineScope(Job() + Dispatchers.Default).launch {
                realmDatabase().query<BluetoothDevice>("connected == true").firstOrNull()?.let {
                    deviceManager.addPairedDeviceIds(it.toSet())
                }
            }
        }
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        setConnectionState(bluetoothDevice, true)
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun didDiscoverDevice(device: BluetoothDevice) {

    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
    }

    override fun isScanning(boolean: Boolean) {
    }

    override fun onBluetoothStateChange(bluetoothState: BluetoothState) {
    }

    fun shouldConnectToDiscoveredDevice(device: BluetoothDevice, onResult: (Boolean) -> Unit) {
        launch {
            device.address?.let { address ->
                getAllDevicesWithAutoReconnectEnabled().firstOrNull()?.let {
                    val addresses = it.filter { it.shouldAutomaticallyReconnect && !it.connected }
                        .mapNotNull { it.address }
                    Napier.i { "Connected Device List with automatic reconnection: $it" }
                    if (address in addresses) {
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
            } ?: kotlin.run {
                onResult(false)
            }
        }
    }

    fun resetAll() {
        bluetoothConnector?.removeObserver(this)
    }
}