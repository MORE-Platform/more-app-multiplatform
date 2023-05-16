package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.clear
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BluetoothDeviceRepository(private val bluetoothConnector: BluetoothConnector? = null) : Repository<BluetoothDevice>(), BluetoothConnectorObserver {
    val connectedDevices = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    private val pairedDeviceIds = mutableSetOf<String>()
    override fun count(): Flow<Long> = realmDatabase().count<BluetoothDevice>()

    fun listenForConnectedDevices() {
        launch {
            getConnectedDevices().cancellable().collect {
                connectedDevices.set(it.toSet())
            }
        }
    }

    fun setConnectionState(bluetoothDevice: BluetoothDevice, connected: Boolean) {
        launch {
            realm()?.write {
                bluetoothDevice.address?.let {
                    val device = this.query<BluetoothDevice>("address = $0", it).first().find()
                    if (device != null) {
                        Napier.i { "Setting state for device: $device to $connected" }
                        device.connected = connected
                    } else {
                        Napier.i { "Adding device $bluetoothDevice and setting state to $connected" }
                        bluetoothDevice.connected = connected
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

    fun getConnectedDevices(connected: Boolean = true) = realmDatabase().query<BluetoothDevice>(query = "connected = $0", queryArgs = arrayOf(connected))

    fun connectedDevicesChange(connected: Boolean, provideNewState: (List<BluetoothDevice>) -> Unit) = getConnectedDevices(connected).asClosure(provideNewState)

    fun getConnectedDevices(provideNewState: (Set<BluetoothDevice>) -> Unit) = connectedDevices.asClosure(provideNewState)

    fun removeDevices(deviceIds: Set<BluetoothDevice>) {
        realmDatabase().deleteItems(deviceIds)
    }

    fun updateConnectedDevices() {
        bluetoothConnector?.let { bluetoothConnector ->
            val context = this
            CoroutineScope(Job() + Dispatchers.Default).launch {
                realmDatabase().query<BluetoothDevice>().firstOrNull()?.let {
                    pairedDeviceIds.addAll(it.mapNotNull { it.address })
                    if (pairedDeviceIds.isNotEmpty()) {
                        bluetoothConnector.observer = context
                        bluetoothConnector.scan()
                        delay(5000)
                        bluetoothConnector.close()
                        setConnectionState(pairedDeviceIds, false)
                        pairedDeviceIds.clear()
                    }
                }
            }
        }
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        setConnectionState(bluetoothDevice, true)
        bluetoothDevice.address?.let {
            pairedDeviceIds.remove(it)
        }
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
    }

    override fun discoveredDevice(device: BluetoothDevice) {
        device.address?.let {
            if (it in pairedDeviceIds) {
                bluetoothConnector?.connect(device)
            }
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
    }
}