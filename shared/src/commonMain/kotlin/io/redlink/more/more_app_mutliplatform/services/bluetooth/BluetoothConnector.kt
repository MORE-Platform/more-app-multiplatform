package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.ktor.utils.io.core.*

interface BluetoothConnector: BluetoothConnectorObserver, Closeable {

    var observer: MutableSet<BluetoothConnectorObserver>

    val connected: MutableSet<BluetoothDevice>

    val discovered: MutableSet<BluetoothDevice>

    var bluetoothState: BluetoothState

    var scanning: Boolean

    val specificBluetoothConnectors: MutableMap<String, BluetoothConnector>

    fun addSpecificBluetoothConnector(key: String, connector: BluetoothConnector)

    fun addObserver(bluetoothConnectorObserver: BluetoothConnectorObserver)

    fun removeObserver(bluetoothConnectorObserver: BluetoothConnectorObserver)

    fun updateObserver(action: (BluetoothConnectorObserver) -> Unit)

    fun replayStates()

    fun scan()

    fun connect(device: BluetoothDevice): Error?

    fun disconnect(device: BluetoothDevice)

    fun stopScanning()
    override fun close()
}