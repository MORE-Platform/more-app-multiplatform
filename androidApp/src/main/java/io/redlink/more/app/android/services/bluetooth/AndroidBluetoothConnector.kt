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
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice

class AndroidBluetoothConnector(private val context: Context): BluetoothConnector {
    override val specificBluetoothConnectors: Map<String, BluetoothConnector> = mapOf("polar" to PolarConnector(context))
    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false
    private var isConnecting = false

    private val foundBluetoothDevices = mutableSetOf<String>()

    override var observer: BluetoothConnectorObserver? = null

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (isScanning) {
                result?.device?.let { device ->
                    device.name?.let {
                        if (device.address !in foundBluetoothDevices) {
                            foundBluetoothDevices.add(device.address)
                            Napier.i { "New Device with name discovered: $it" }
                            val bluetoothDevice = device.toBluetoothDevice()
                            if (device.bondState == AndroidBluetoothDevice.BOND_BONDED) {
                                deviceConnected(bluetoothDevice)
                            } else {
                                observer?.discoveredDevice(bluetoothDevice)
                            }
                        }
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            if (AndroidBluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {
                Napier.i { "Bond State changed" }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(AndroidBluetoothDevice.EXTRA_DEVICE, AndroidBluetoothDevice::class.java)
                } else {
                    intent.getParcelableExtra(AndroidBluetoothDevice.EXTRA_DEVICE)
                }?.let { pairedDevice ->
                    val bondState: Int = intent.getIntExtra(AndroidBluetoothDevice.EXTRA_BOND_STATE, AndroidBluetoothDevice.ERROR)
                    val prevBondState: Int = intent.getIntExtra(AndroidBluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, AndroidBluetoothDevice.ERROR)
                    val device = pairedDevice.toBluetoothDevice()
                    Napier.i { "Bond state of device $device: $bondState. ${pairedDevice.bondState}" }
                    if (bondState == AndroidBluetoothDevice.BOND_BONDED && prevBondState != AndroidBluetoothDevice.BOND_BONDED) {
                        deviceConnected(device)
                    } else if (bondState == AndroidBluetoothDevice.BOND_NONE) {
                        isConnecting = false
                        disconnect(device)
                        foundBluetoothDevices.remove(device.address)
                    } else if (bondState == AndroidBluetoothDevice.BOND_BONDING) {
                        observer?.isConnectingToDevice(device)
                    } else {}
                }
            }
        }
    }

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                specificBluetoothConnectors.values.forEach { it.observer = this }
                bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                val bondStateChangedFilter = IntentFilter(AndroidBluetoothDevice.ACTION_BOND_STATE_CHANGED)
                context.registerReceiver(bondStateReceiver, bondStateChangedFilter)
            } else {
                Napier.i { "Bluetooth Adapter not enabled!" }
            }
        } else {
            Napier.e { "Bluetooth permissions not given!" }
        }
    }


    @SuppressLint("MissingPermission")
    override fun scan() {
        if (!isScanning && !isConnecting) {
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    override fun connect(device: BluetoothDevice): Error? {
        Napier.i { "Connecting to device: $device" }
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
                Napier.i { "Device already has bond state! Bonding..." }
                androidBluetoothDevice.createBond()
            } else {
                val gattCallback = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                        Napier.i { "Connection to $device: $newState" }
                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                deviceConnected(device)
                            }
                            BluetoothProfile.STATE_CONNECTING -> {
                                observer?.isConnectingToDevice(device)
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                if (status == BluetoothProfile.STATE_CONNECTED || status == BluetoothProfile.STATE_DISCONNECTING) {
                                    deviceConnected(device)
                                } else if (status == BluetoothProfile.STATE_CONNECTING || status == BluetoothProfile.STATE_DISCONNECTED) {
                                    Napier.i { "Problem connecting" }
                                    observer?.didFailToConnectToDevice(device)
                                    isConnecting = false
                                }
                                gatt.close()
                            }
                        }
                    }
                }
                androidBluetoothDevice.connectGatt(context, false, gattCallback)
            }
            return null
        } else {
            observer?.didFailToConnectToDevice(device)
        }
        return Error("Could not find device")
    }

    override fun disconnect(device: BluetoothDevice) {
        if (!disconnectFromSpecificConnectors(device)) {
            val androidBluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            if (androidBluetoothDevice != null) {
                observer?.didDisconnectFromDevice(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        if (isScanning) {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
        }
    }

    override fun isScanning(): Boolean {
        return isScanning
    }

    override fun close() {
        stopScanning()
        foundBluetoothDevices.clear()
        specificBluetoothConnectors.values.forEach { it.close() }

        context.unregisterReceiver(bondStateReceiver)
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.isConnectingToDevice(bluetoothDevice)
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didConnectToDevice(bluetoothDevice)
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        foundBluetoothDevices.remove(bluetoothDevice.address)
        observer?.didDisconnectFromDevice(bluetoothDevice)
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        observer?.didFailToConnectToDevice(bluetoothDevice)
        isConnecting = false
    }

    override fun discoveredDevice(device: BluetoothDevice) {
        observer?.discoveredDevice(device)
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        observer?.removeDiscoveredDevice(device)
    }

    private fun deviceConnected(bluetoothDevice: BluetoothDevice) {
        val (hasSpecialConnector, error) = connectToSpecificConnectors(bluetoothDevice)
        if (hasSpecialConnector) {
            if (error != null) {
                Napier.e { error.stackTraceToString() }
            }
        } else {
            observer?.didConnectToDevice(bluetoothDevice)
        }
        isConnecting = false
    }

    private fun connectToSpecificConnectors(device: BluetoothDevice): Pair<Boolean, Error?> {
        return specificBluetoothConnectors.keys.firstOrNull { device.deviceName?.lowercase()?.contains(it) ?: false }?.let {
            Pair(true,specificBluetoothConnectors[it]?.connect(device))
        } ?: Pair(false, null)
    }

    private fun disconnectFromSpecificConnectors(device: BluetoothDevice): Boolean {
        return specificBluetoothConnectors.keys.firstOrNull{device.deviceName?.lowercase()?.contains(it) ?: false}?.let {
            specificBluetoothConnectors[it]?.disconnect(device)
            true
        } ?: false
    }
}

@SuppressLint("MissingPermission")
fun AndroidBluetoothDevice.toBluetoothDevice(): BluetoothDevice {
    return BluetoothDevice.create(this.address, this.name, this.address, true)
}