package io.redlink.more.more_app_mutliplatform.database.repository

import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BluetoothDeviceRepository(private val bluetoothConnector: BluetoothConnector? = null) : Repository<BluetoothDevice>(), BluetoothConnectorObserver {
    private val pairedDeviceIds = mutableSetOf<String>()
    override fun count(): Flow<Long> = realmDatabase.count<BluetoothDevice>()
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    fun setConnectionState(bluetoothDevice: BluetoothDevice, connected: Boolean) {
        scope.launch {
            realmDatabase.realm?.write {
                bluetoothDevice.address?.let {
                    val device = this.query<BluetoothDevice>("address = $0", it).first().find()
                    if (device != null) {
                        device.connected = connected
                    } else {
                        bluetoothDevice.connected = connected
                        this.copyToRealm(bluetoothDevice, updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        }
    }

    fun setAllConnectionStates(connected: Boolean) {
        realmDatabase.realm?.writeBlocking {
            this.query<BluetoothDevice>()
                .find()
                .map {
                    it.connected = connected
                }
        }
    }

    fun setConnectionState(deviceIds: Set<String>, connected: Boolean) {
        realmDatabase.realm?.writeBlocking {
            this.query<BluetoothDevice>()
                .find()
                .filter { it.deviceId in deviceIds }
                .map {
                    it.connected = connected
                }
        }
    }

    fun getDevices() = realmDatabase.query<BluetoothDevice>()

    fun getConnectedDevices(connected: Boolean = true) = realmDatabase.query<BluetoothDevice>(query = "connected = $0", queryArgs = arrayOf(connected))

    fun removeDevices(deviceIds: Set<BluetoothDevice>) {
        realmDatabase.deleteItems(deviceIds)
    }

    fun updateConnectedDevices() {
        bluetoothConnector?.let { bluetoothConnector ->
            val context = this
            CoroutineScope(Job() + Dispatchers.Default).launch {
                realmDatabase.query<BluetoothDevice>().firstOrNull()?.let {
                    pairedDeviceIds.addAll(it.mapNotNull { it.deviceId })
                    if (pairedDeviceIds.isNotEmpty()) {
                        bluetoothConnector.observer = context
                        bluetoothConnector.scan()
                        delay(2000)
                        bluetoothConnector.close()
                        setConnectionState(pairedDeviceIds, false)
                        pairedDeviceIds.clear()
                    }
                }
            }
        }
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        setConnectionState(bluetoothDevice, true)
        bluetoothDevice.deviceId?.let {
            pairedDeviceIds.remove(it)
        }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun discoveredDevice(device: BluetoothDevice) {
        device.deviceId?.let {
            if (it in pairedDeviceIds) {
                bluetoothConnector?.connect(device)
            }
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
    }
}