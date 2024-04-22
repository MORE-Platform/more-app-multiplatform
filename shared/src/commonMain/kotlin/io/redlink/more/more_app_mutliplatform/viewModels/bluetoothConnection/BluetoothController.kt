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
package io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.more_app_mutliplatform.extensions.anyNameIn
import io.redlink.more.more_app_mutliplatform.extensions.areAllNamesIn
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import io.redlink.more.more_app_mutliplatform.util.Scope.repeatedLaunch
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull

class BluetoothController(
    private val bluetoothConnector: BluetoothConnector,
    private val scanDuration: Long = 10000,
    private val scanInterval: Long = 5000
) : CoreViewModel(), BluetoothConnectorObserver, Closeable {
    val showBluetoothView = MutableStateFlow(false);
    private val deviceManager = BluetoothDeviceManager
    private val bluetoothDeviceRepository = BluetoothDeviceRepository(bluetoothConnector)

    val isScanning = MutableStateFlow(false)

    private var backgroundScanningEnabled = false
    private var viewActive = false

    private var scanJob: String? = null

    val bluetoothPower = MutableStateFlow(BluetoothState.ON)

    init {
        bluetoothConnector.addObserver(this)
        bluetoothConnector.replayStates()
    }

    fun observerDeviceAccessible(bleDevices: Set<String>): Boolean {
        val pairedDevices = deviceManager.pairedDevices.value
        val connectedDevices = deviceManager.connectedDevices.value;
        if (bleDevices.anyNameIn(pairedDevices)) {
            if (bleDevices.anyNameIn(connectedDevices)) {
                disableBackgroundScanner()
                return true
            } else {
                enableBackgroundScanner()
            }
        } else {
            showBluetoothView.value = true
        }
        return false
    }

    fun observationDeviceConnector(observationFactory: ObservationFactory) {
        val neededBLEDeviceIdentifier = observationFactory.bleDevicesNeeded()
        val pairedDevices = deviceManager.pairedDevices.value
        if (neededBLEDeviceIdentifier.isNotEmpty()
            && neededBLEDeviceIdentifier.any { name ->
                pairedDevices.any {
                    it.deviceName?.contains(
                        name
                    ) == true
                }
            }
        ) {
            launch {
                deviceManager.connectedDevices.collect { connectedDevices ->
                    if (!neededBLEDeviceIdentifier.areAllNamesIn(connectedDevices)) {
                        enableBackgroundScanner()
                    }
                }
            }
            launch {
                deviceManager.discoveredDevices.collect { discoveredDevices ->

                }
            }
        }
    }

    fun enableBackgroundScanner() {
        if (!backgroundScanningEnabled) {
            backgroundScanningEnabled = true
            Scope.launch {
                if (!viewActive && bluetoothDeviceRepository.getDevices().firstOrNull()
                        ?.isNotEmpty() == true
                ) {
                    delay(2000)
                    periodicScan(BACKGROUND_SCAN_DURATION, BACKGROUND_SCAN_INTERVAL)
                }
            }
        }
    }

    fun disableBackgroundScanner() {
        backgroundScanningEnabled = false
        if (!viewActive) {
            stopPeriodicScan()
        }
    }

    override fun viewDidAppear() {
        viewActive = true
        if (backgroundScanningEnabled) {
            stopPeriodicScan()
        }
        periodicScan()
    }

    private fun periodicScan(
        customScanDuration: Long = scanDuration,
        customScanInterval: Long = scanInterval
    ) {
        launchScope {
            bluetoothPower.collect {
                if (it == BluetoothState.ON) {
                    startPeriodicScan(customScanDuration, customScanInterval)
                } else {
                    stopPeriodicScan()
                }
            }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        viewActive = false
        scanJob?.let { Scope.cancel(it) }
        scanJob = null
        stopPeriodicScan()
        if (backgroundScanningEnabled) {
            Scope.launch {
                delay(10000L)
                if (backgroundScanningEnabled) {
                    periodicScan(BACKGROUND_SCAN_DURATION, BACKGROUND_SCAN_INTERVAL)
                }
            }
        }
    }

    private fun startPeriodicScan(
        customScanDuration: Long = scanDuration,
        customScanInterval: Long = scanInterval
    ) {
        if (scanJob == null) {
            Napier.i(tag = "BluetoothController::startPeriodicScan") { "Starting period scanner with Duration= $customScanDuration; Interval= $customScanInterval" }
            scanJob = repeatedLaunch(customScanInterval) {
                Napier.i(tag = "BluetoothController::startPeriodicScan") { "Scanning..." }
                scanForDevices()
                delay(customScanDuration)
                Napier.i(tag = "BluetoothController::startPeriodicScan") { "Stop Scanning..." }
                stopScanning()
            }.first
        }
    }

    fun stopPeriodicScan() {
        Napier.i(tag = "BluetoothController::stopPeriodicScan") { "Stopping period scanner!" }
        scanJob?.let { Scope.cancel(it) }
        scanJob = null
        bluetoothConnector.stopScanning()
    }

    fun scanForDevices() {
        bluetoothConnector.scan()
    }

    fun stopScanning() {
        bluetoothConnector.stopScanning()
        deviceManager.foreachConnectedDevice {
            didDisconnectFromDevice(it)
        }
        deviceManager.foreachDiscoveredDevice {
            removeDiscoveredDevice(it)
        }
    }

    fun connectToDevice(device: BluetoothDevice): Boolean {
        Napier.i(tag = "BluetoothController::connectToDevice") { "Connecting to $device" }
        deviceManager.addConnectingDevices(setOf(device))
        val hasError = bluetoothConnector.connect(device) == null
        return hasError
    }

    fun disconnectFromDevice(device: BluetoothDevice) {
        Napier.i(tag = "BluetoothController::disconnectFromDevice") { "Disconnecting from $device" }
        deviceManager.removeConnectedDevices(setOf(device))
        bluetoothConnector.disconnect(device)
        bluetoothDeviceRepository.setAutoReconnect(device, false)
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDevice) {
        deviceManager.addConnectingDevices(setOf(bluetoothDevice))
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDevice) {
        deviceManager.addConnectedDevices(setOf(bluetoothDevice))
        bluetoothDeviceRepository.setConnectionState(bluetoothDevice, true)
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDevice) {
        Napier.i(tag = "BluetoothController::didDisconnectFromDevice") { "Disconnected from $bluetoothDevice" }
        deviceManager.removeConnectedDevices(setOf(bluetoothDevice))
        bluetoothDeviceRepository.setConnectionState(bluetoothDevice, false)
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDevice) {
        Napier.e(tag = "BluetoothController::didFailToConnectToDevice") { "Failed to connect to $bluetoothDevice" }
        deviceManager.removeConnectingDevices(setOf(bluetoothDevice))
        bluetoothDeviceRepository.setConnectionState(bluetoothDevice, false)
    }

    override fun onBluetoothStateChange(bluetoothState: BluetoothState) {
        Napier.i(tag = "BluetoothController::onBluetoothStateChange") { "Bluetooth state changed to $bluetoothState" }
        bluetoothPower.set(bluetoothState)
    }

    override fun didDiscoverDevice(device: BluetoothDevice) {
        Napier.i(tag = "BluetoothController::didDiscoverDevice") { "Discovered device: $device" }
        deviceManager.addDiscoveredDevices(setOf(device))
        bluetoothDeviceRepository.shouldConnectToDiscoveredDevice(device) {
            if (it) {
                connectToDevice(device)
            }
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDevice) {
        Napier.i(tag = "BluetoothController::removeDiscoveredDevice") { "Removed discovered device: $device" }
        deviceManager.removeDiscoveredDevices(setOf(device))
    }

    override fun isScanning(boolean: Boolean) {
        Napier.i(tag = "BluetoothController::isScanning") { "Scanning status changed to: $boolean" }
        this.isScanning.set(boolean)
    }


    fun bluetoothStateChanged(providedState: (BluetoothState) -> Unit) =
        bluetoothPower.asClosure(providedState)

    fun resetAll() {
        Napier.i(tag = "BluetoothController::resetAll") { "Resetting Bluetooth data!" }
        stopPeriodicScan()
        close()
        isScanning.set(false)
        backgroundScanningEnabled = false
        viewActive = false
        deviceManager.resetAll()
        bluetoothDeviceRepository.resetAll()
        scanJob = null
    }

    override fun close() {
        scanJob?.let { Scope.cancel(it) }
        scanJob = null
        bluetoothConnector.stopScanning()
    }

    companion object {
        private const val BACKGROUND_SCAN_DURATION = 2000L
        private const val BACKGROUND_SCAN_INTERVAL = 10000L
    }
}