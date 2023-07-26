package io.redlink.more.app.android.services.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier
import io.github.aakira.napier.Napier.i
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice

class AndroidBluetoothConnector(context: Context): BluetoothConnector {
    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isConnecting = false
    private val foundBluetoothDevices = mutableSetOf<String>()

    override val specificBluetoothConnectors: MutableMap<String, BluetoothConnector> = mutableMapOf()
    override var observer: MutableSet<BluetoothConnectorObserver> = mutableSetOf()
    override var scanning = false
    override val connected: MutableSet<BluetoothDevice> = mutableSetOf()
    override val discovered: MutableSet<BluetoothDevice> = mutableSetOf()
    override var bluetoothState: BluetoothState = if (bluetoothAdapter?.isEnabled == true) BluetoothState.ON else BluetoothState.OFF

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (scanning) {
                result?.device?.let { device ->
                    device.name?.let {
                        if (device.address !in foundBluetoothDevices) {
                            foundBluetoothDevices.add(device.address)
                            i { "New Device with name discovered: $it" }
                            val bluetoothDevice = device.toBluetoothDevice()
                            if (device.bondState == AndroidBluetoothDevice.BOND_BONDED) {
                                deviceConnected(bluetoothDevice)
                            } else {
                                didDiscoverDevice(bluetoothDevice)
                            }
                        }
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Napier.e { "Scanning failed with code: $errorCode" }
            isScanning(false)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            gatt?.device?.toBluetoothDevice()?.let { device ->
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        deviceConnected(device)
                    }
                    BluetoothProfile.STATE_CONNECTING -> {
                        isConnectingToDevice(device)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        if (status == BluetoothProfile.STATE_CONNECTING) {
                            i { "Problem connecting to device: $device" }
                            didFailToConnectToDevice(device)
                        }
                        else {
                            didDisconnectFromDevice(device)
                        }
                        gatt.close()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when {
                AndroidBluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action -> {
                    i { "Bond State changed" }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(AndroidBluetoothDevice.EXTRA_DEVICE, AndroidBluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(AndroidBluetoothDevice.EXTRA_DEVICE)
                    }?.let { pairedDevice ->
                        val bondState: Int = intent.getIntExtra(AndroidBluetoothDevice.EXTRA_BOND_STATE, AndroidBluetoothDevice.ERROR)
                        val prevBondState: Int = intent.getIntExtra(AndroidBluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, AndroidBluetoothDevice.ERROR)
                        val device = pairedDevice.toBluetoothDevice()
                        i { "Bond state of device $device: $bondState. ${pairedDevice.bondState}" }
                        if (bondState == AndroidBluetoothDevice.BOND_BONDED && prevBondState != AndroidBluetoothDevice.BOND_BONDED) {
                            deviceConnected(device)
                        } else if (bondState == AndroidBluetoothDevice.BOND_NONE) {
                            isConnecting = false
                            disconnect(device)
                            foundBluetoothDevices.remove(device.address)
                        } else if (bondState == AndroidBluetoothDevice.BOND_BONDING) {
                            isConnectingToDevice(device)
                        } else {}
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED == intent.action -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_OFF -> {
                            onBluetoothStateChange(BluetoothState.OFF)
                        }
                        BluetoothAdapter.STATE_ON -> {
                            onBluetoothStateChange(BluetoothState.ON)
                        }
                    }
                }
            }
        }
    }

    init {
        if (ContextCompat.checkSelfPermission(MoreApplication.appContext!!, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(MoreApplication.appContext!!, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(MoreApplication.appContext!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                specificBluetoothConnectors.values.forEach {
                    it
                    it.bluetoothState = this.bluetoothState
                }
                val bondStateChangedFilter = IntentFilter()
                bondStateChangedFilter.addAction(AndroidBluetoothDevice.ACTION_BOND_STATE_CHANGED)
                bondStateChangedFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                MoreApplication.appContext!!.registerReceiver(bondStateReceiver, bondStateChangedFilter)
            } catch (exception: Exception) {
                Napier.w { exception.stackTraceToString() }
            }

        } else {
            Napier.e { "Bluetooth permissions not given!" }
        }
    }

    override fun addSpecificBluetoothConnector(key: String, connector: BluetoothConnector) {
        specificBluetoothConnectors[key] = connector
    }

    override fun addObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {
        this.observer.add(bluetoothConnectorObserver)
        if (observer.isNotEmpty()) {
            replayStates()
        }
    }

    override fun removeObserver(bluetoothConnectorObserver: BluetoothConnectorObserver) {
        this.observer.remove(bluetoothConnectorObserver)
        if (observer.isEmpty()) {
            stopScanning()
        }
    }

    override fun updateObserver(action: (BluetoothConnectorObserver) -> Unit) {
        observer.forEach(action)
    }

    override fun replayStates() {
        connected.forEach { didConnectToDevice(it) }
        discovered.forEach { didDiscoverDevice(it) }
        isScanning(this.scanning)
        onBluetoothStateChange(this.bluetoothState)
    }

    @SuppressLint("MissingPermission")
    override fun scan() {
        if (bluetoothState == BluetoothState.ON && observer != null && !scanning && !isConnecting) {
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
            isScanning(true)
        }
    }

    @SuppressLint("MissingPermission")
    override fun connect(device: BluetoothDevice): Error? {
        if (bluetoothState == BluetoothState.ON ) {
            i { "Connecting to device: $device" }
            isConnecting = true
            stopScanning()
            val (hasSpecialConnector, error) = connectToSpecificConnectors(device)
            if (hasSpecialConnector) {
                isConnecting = false
                return error
            }
            val androidBluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            if (androidBluetoothDevice != null) {
                if (androidBluetoothDevice.bondState == AndroidBluetoothDevice.BOND_NONE) {
                    i { "Device already has bond state! Bonding..." }
                    androidBluetoothDevice.createBond()
                } else {
                    MoreApplication.appContext?.let {
                        androidBluetoothDevice.connectGatt(it, false, gattCallback)
                    }
                }
                return null
            } else {
                observer?.didFailToConnectToDevice(device)
            }
            return Error("Could not find device")
        }
        return Error("Bluetooth disabled!")
    }

    @SuppressLint("MissingPermission")
    override fun disconnect(device: BluetoothDevice) {
        if (!disconnectFromSpecificConnectors(device)) {
            val androidBluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            if (androidBluetoothDevice != null) {
                val gatt = androidBluetoothDevice.connectGatt(MoreApplication.appContext!!, false, gattCallback)
                gatt.disconnect()
            }
        } else {
            observer?.didDisconnectFromDevice(device)
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        if (scanning) {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning(false)
        }
    }

    override fun close() {
        stopScanning()
        foundBluetoothDevices.clear()
        connected.clear()
        discovered.clear()
        specificBluetoothConnectors.values.forEach { it.close() }

        try {
            MoreApplication.appContext?.unregisterReceiver(bondStateReceiver)
        } catch (exception: Exception) {
            Napier.w { exception.stackTraceToString() }
        }
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        i { "Connecting to $bluetoothDevice..." }
        observer?.isConnectingToDevice(bluetoothDevice)
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        i { "Connected to $bluetoothDevice!" }
        if (!connected.mapNotNull { it.address }.contains(bluetoothDevice.address)) {
            connected.add(bluetoothDevice)
            removeDiscoveredDevice(bluetoothDevice)
        }
        observer?.didConnectToDevice(bluetoothDevice)
        isConnecting = false
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        i { "Disconnected from $bluetoothDevice!" }
        connected.removeAll { it.address == bluetoothDevice.address }
        foundBluetoothDevices.remove(bluetoothDevice.address)
        observer?.didDisconnectFromDevice(bluetoothDevice)
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        i { "Failed to connect to $bluetoothDevice!" }
        observer?.didFailToConnectToDevice(bluetoothDevice)
        isConnecting = false
    }

    override fun didDiscoverDevice(device: BluetoothDevice) {
        i { "Discovered $device!" }
        if (!discovered.mapNotNull { it.address }.contains(device.address)) {
            discovered.add(device)
        }
        observer?.didDiscoverDevice(device)
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        i { "Removing discovered $device..." }
        discovered.removeAll { it.address == device.address }
        observer?.removeDiscoveredDevice(device)
    }

    override fun onBluetoothStateChange(bluetoothState: BluetoothState) {
        i { "Bluetooth State changed to: $bluetoothState" }
        this.bluetoothState = bluetoothState
        if (bluetoothState == BluetoothState.ON) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            } else {
                i { "Bluetooth Adapter not enabled!" }
            }
        }
        observer?.onBluetoothStateChange(bluetoothState)
    }

    override fun isScanning(boolean: Boolean) {
        this.scanning = boolean
        observer?.isScanning(boolean)
    }

    private fun deviceConnected(bluetoothDevice: BluetoothDevice) {
        val (hasSpecialConnector, error) = connectToSpecificConnectors(bluetoothDevice)
        if (hasSpecialConnector) {
            if (error != null) {
                Napier.e { error.stackTraceToString() }
            }
        }
        didConnectToDevice(bluetoothDevice)
    }

    private fun connectToSpecificConnectors(device: BluetoothDevice): Pair<Boolean, Error?> {
        return specificBluetoothConnectors.keys.firstOrNull { device.deviceName?.lowercase()?.contains(it) ?: false }?.let {
            i { "Connecting with special connector \"$it\"..." }
            Pair(true,specificBluetoothConnectors[it]?.connect(device))
        } ?: Pair(false, null)
    }

    private fun disconnectFromSpecificConnectors(device: BluetoothDevice): Boolean {
        return specificBluetoothConnectors.keys.firstOrNull {
            i { "Disconnecting with special connector \"$it\"..." }
            device.deviceName?.lowercase()?.contains(it) ?: false
        }?.let {
            specificBluetoothConnectors[it]?.disconnect(device)
            true
        } ?: false
    }


}

@SuppressLint("MissingPermission")
fun AndroidBluetoothDevice.toBluetoothDevice(): BluetoothDevice {
    return BluetoothDevice.create(this.address, this.name, this.address, true)
}