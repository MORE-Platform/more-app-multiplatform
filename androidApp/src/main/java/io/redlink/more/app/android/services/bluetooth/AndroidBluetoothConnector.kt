package io.redlink.more.app.android.services.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import java.io.Closeable
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice

private const val TAG = "AndroidBluetoothConnector"

class AndroidBluetoothConnector(private val context: Context): BluetoothConnector {
    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false

    override var observer: BluetoothConnectorObserver? = null

    val deviceList = mutableSetOf<BluetoothDevice>()
    val connectedDevices = mutableSetOf<BluetoothDevice>()

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            } else {
            }
        } else {
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let { device ->
                device.name?.let {
                    val bluetoothDevice = BluetoothDevice(
                        deviceId = device.address,
                        deviceName = it,
                        address = device.address,
                        isConnectable = true
                    )
                    deviceList.add(bluetoothDevice)
                    observer?.discoveredBLEUpdated(deviceList)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    @SuppressLint("MissingPermission")
    override fun scan() {
        if (!isScanning) {
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    override fun connect(device: BluetoothDevice): Error? {
        val androidBluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
        if (androidBluetoothDevice != null) {
            if (androidBluetoothDevice.bondState == AndroidBluetoothDevice.BOND_NONE) {
                androidBluetoothDevice.createBond()
                observer?.didConnectToDevice(device)
            } else {
                val gattCallback = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                connectedDevices.add(device)
                                observer?.didConnectToDevice(device)
                                observer?.connectedBLEUpdated(deviceList)
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                connectedDevices.remove(device)
                                observer?.connectedBLEUpdated(deviceList)
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
            return Error("Could not find device")
        }
    }

    override fun disconnect(device: BluetoothDevice) {
        val androidBluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
        if (androidBluetoothDevice != null) {
            connectedDevices.remove(device)
            observer?.connectedBLEUpdated(connectedDevices)
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopScanning() {
        if (isScanning) {
            bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
        }
    }

    override fun close() {
        stopScanning()
        observer = null
    }

    override fun isScanning(): Boolean {
        return isScanning
    }
}