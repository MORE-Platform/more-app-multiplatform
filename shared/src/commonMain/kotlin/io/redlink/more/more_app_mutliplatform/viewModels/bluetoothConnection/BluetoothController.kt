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

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.entities.BluetoothDeviceEntity
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.more_app_mutliplatform.extensions.anyNameIn
import io.redlink.more.more_app_mutliplatform.extensions.areAllNamesIn
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.scopes.Scope
import io.redlink.more.more_app_mutliplatform.scopes.StudyScope
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDeviceManager
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.ViewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull

class BluetoothController(
    private val bluetoothDeviceRepository: BluetoothDeviceRepository,
    private val bluetoothConnector: BluetoothConnector,
    private val scanDuration: Long = 5000,
    private val scanInterval: Long = 10000
) : CoreViewModel(), BluetoothConnectorObserver, Closeable {
    private val deviceManager = BluetoothDeviceManager

    private val _isScanning = MutableStateFlow(false)

    @NativeCoroutines
    val isScanning: StateFlow<Boolean> = _isScanning

    private var backgroundScanningEnabled = false
    private var viewActive = false

    private var scanJob: String? = null

    private val _bluetoothPower = MutableStateFlow(BluetoothState.ON)

    @NativeCoroutines
    val bluetoothPower: StateFlow<BluetoothState> = _bluetoothPower

    private var bleViewHasBeenOpened = false

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
            if (!bleViewHasBeenOpened) {
                if (ViewManager.showBLEView(true)) {
                    bleViewHasBeenOpened = true
                }
            }
        }
        return false
    }

    fun startScanningForDevices(bleDeviceSet: Set<String>) {
        if (bleDeviceSet.isNotEmpty()) {
            val pairedDevices = deviceManager.pairedDevices.value
            if (bleDeviceSet.areAllNamesIn(pairedDevices)) {
                val connectedDevices = deviceManager.connectedDevices.value
                if (bleDeviceSet.areAllNamesIn(connectedDevices)) {
                    disableBackgroundScanner()
                } else {
                    enableBackgroundScanner()
                }
            } else {
                if (!bleViewHasBeenOpened) {
                    if (ViewManager.showBLEView(true)) {
                        bleViewHasBeenOpened = true
                    }
                }
            }
        }
    }

    private fun enableBackgroundScanner() {
        if (!backgroundScanningEnabled) {
            backgroundScanningEnabled = true
            StudyScope.launch {
                if (!viewActive && bluetoothDeviceRepository.pairedDevices().firstOrNull()
                        ?.isNotEmpty() == true
                ) {
                    delay(2000)
                    periodicScan(BACKGROUND_SCAN_DURATION, BACKGROUND_SCAN_INTERVAL)
                }
            }
        }
    }

    private fun disableBackgroundScanner() {
        backgroundScanningEnabled = false
        if (!viewActive) {
            stopPeriodicScan()
        }
    }

    override fun viewDidAppear() {
        viewActive = true
        bleViewHasBeenOpened = true
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
            _bluetoothPower.collect {
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
        scanJob?.let { StudyScope.cancel(it) }
        scanJob = null
        stopPeriodicScan()
        if (backgroundScanningEnabled) {
            StudyScope.launch {
                delay(10000L)
                if (backgroundScanningEnabled) {
                    periodicScan(BACKGROUND_SCAN_DURATION, BACKGROUND_SCAN_INTERVAL)
                }
            }
        } else {
            deviceManager.clearDiscovered()
        }
    }

    private fun startPeriodicScan(
        customScanDuration: Long = scanDuration,
        customScanInterval: Long = scanInterval
    ) {
        if (scanJob == null) {
            Napier.i(tag = "BluetoothController::startPeriodicScan") { "Starting period scanner with Duration= $customScanDuration; Interval= $customScanInterval" }
            scanJob = StudyScope.repeatedLaunch(customScanInterval, Dispatchers.IO) {
                Napier.i(tag = "BluetoothController::startPeriodicScan") { "Scanning..." }
                scanForDevices()
                delay(customScanDuration)
                Napier.i(tag = "BluetoothController::startPeriodicScan") { "Stop Scanning..." }
                stopScanning()
            }.first
        }
    }

    private fun stopPeriodicScan() {
        Napier.i(tag = "BluetoothController::stopPeriodicScan") { "Stopping period scanner!" }
        scanJob?.let { StudyScope.cancel(it) }
        scanJob = null
        bluetoothConnector.stopScanning()
    }

    private fun scanForDevices() {
        bluetoothConnector.scan()
    }

    fun stopScanning() {
        bluetoothConnector.stopScanning()
    }

    fun connectToDevice(device: BluetoothDeviceEntity): Boolean {
        if (!deviceManager.connectedDevices.value.contains(device)) {
            Napier.i(tag = "BluetoothController::connectToDevice") { "Connecting to $device" }
            deviceManager.addConnectingDevices(setOf(device))
            return bluetoothConnector.connect(device) == null
        }
        return true
    }

    fun unpairFromDevice(device: BluetoothDeviceEntity) {
        Napier.i(tag = "BluetoothController::disconnectFromDevice") { "Disconnecting from $device" }
        bluetoothConnector.disconnect(device)
        bluetoothDeviceRepository.unpairDevice(device)
        deviceManager.removePairedDeviceIds(setOf(device))
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        deviceManager.addConnectingDevices(setOf(bluetoothDevice))
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        deviceManager.addConnectedDevices(setOf(bluetoothDevice))
        bluetoothDeviceRepository.storePairedDevice(bluetoothDevice)
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDeviceEntity) {
        Napier.i(tag = "BluetoothController::didDisconnectFromDevice") { "Disconnected from $bluetoothDevice" }
        deviceManager.removeConnectedDevices(setOf(bluetoothDevice))
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        Napier.e(tag = "BluetoothController::didFailToConnectToDevice") { "Failed to connect to $bluetoothDevice" }
        deviceManager.removeConnectingDevices(setOf(bluetoothDevice))
    }

    override fun onBluetoothStateChange(bluetoothState: BluetoothState) {
        Napier.i(tag = "BluetoothController::onBluetoothStateChange") { "Bluetooth state changed to $bluetoothState" }
        _bluetoothPower.set(bluetoothState)
        if (bluetoothState == BluetoothState.OFF) {
            deviceManager.clearDiscovered()
            deviceManager.clearConnected()
            deviceManager.clearConnectingDevices()
        }
    }

    override fun didDiscoverDevice(device: BluetoothDeviceEntity) {
        if (!deviceManager.connectedDevices.value.contains(device)) {
            Napier.i(tag = "BluetoothController::didDiscoverDevice") { "Discovered device: $device" }
            deviceManager.addDiscoveredDevices(setOf(device))
            if (deviceManager.pairedDevices.value.contains(device)) {
                connectToDevice(device)
            }
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDeviceEntity) {
        Napier.i(tag = "BluetoothController::removeDiscoveredDevice") { "Removed discovered device: $device" }
        deviceManager.removeDiscoveredDevices(setOf(device))
    }

    override fun isScanning(boolean: Boolean) {
        Napier.i(tag = "BluetoothController::isScanning") { "Scanning status changed to: $boolean" }
        this._isScanning.set(boolean)
    }

    suspend fun listenToConnectionChanges(
        observationFactory: ObservationFactory
    ) {
        deviceManager.connectedDevices.collect {
            observationFactory.updateObservationErrors()
        }
    }

    fun resetAll() {
        Napier.i(tag = "BluetoothController::resetAll") { "Resetting Bluetooth data!" }
        stopPeriodicScan()
        close()
        _isScanning.set(false)
        backgroundScanningEnabled = false
        viewActive = false
        deviceManager.resetAll()
        scanJob = null
    }

    override fun close() {
        scanJob?.let { Scope.cancel(it) }
        scanJob = null
        bluetoothConnector.stopScanning()
    }

    companion object {
        // Energy-optimized scanning intervals
        private const val BACKGROUND_SCAN_DURATION = 1000L  // Reduced from 2s to 1s
        private const val BACKGROUND_SCAN_INTERVAL =
            30000L // Increased from 10s to 30s for better battery life
        private const val MAX_BACKGROUND_SCAN_INTERVAL = 300000L // Max 5 minutes between scans
    }
}
