package io.redlink.more.more_app_mutliplatform.services.bluetooth

import io.ktor.utils.io.core.*

interface BluetoothConnector: BluetoothConnectorObserver, Closeable {

    val specificBluetoothConnectors: Map<String, BluetoothConnector>
        get() = emptyMap()

    var observer: BluetoothConnectorObserver?

    fun scan()

    fun connect(device: BluetoothDevice): Error?

    fun disconnect(device: BluetoothDevice)

    fun stopScanning()

    fun isScanning(): Boolean

    override fun close()
}