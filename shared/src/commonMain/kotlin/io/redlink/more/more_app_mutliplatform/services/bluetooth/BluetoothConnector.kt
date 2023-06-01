package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.ktor.utils.io.core.*

interface BluetoothConnector: BluetoothConnectorObserver, Closeable {

    var observer: BluetoothConnectorObserver?

    val connected: MutableSet<BluetoothDevice>

    val discovered: MutableSet<BluetoothDevice>

    var bluetoothState: BluetoothState

    var scanning: Boolean

    val specificBluetoothConnectors: Map<String, BluetoothConnector>
        get() = emptyMap()

    fun applyObserver(bluetoothConnectorObserver: BluetoothConnectorObserver?)

    fun replayStates()

    fun scan()

    fun connect(device: BluetoothDevice): Error?

    fun disconnect(device: BluetoothDevice)

    fun stopScanning()
    override fun close()
}