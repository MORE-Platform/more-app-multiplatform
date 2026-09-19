/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.viewModels.bluetoothConnection

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.database.repository.BluetoothDeviceRepository
import io.redlink.more.extensions.anyNameIn
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothConnector
import io.redlink.more.services.bluetooth.BluetoothConnectorObserver
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import io.redlink.more.services.bluetooth.ScanMode
import io.redlink.more.viewModels.CoreViewModel
import io.redlink.more.viewModels.ViewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class BluetoothController(
    private val bluetoothDeviceRepository: BluetoothDeviceRepository,
    private val bluetoothConnector: BluetoothConnector,
    private val scanDuration: Long = 5000,
    private val scanInterval: Long = 10000,
    observationFactory: ObservationFactory
) : CoreViewModel(), BluetoothConnectorObserver, Closeable {
    private val bleManager = BluetoothStateManagement
    private val supervisor = SupervisorJob()

    private var periodicScanJob: Job? = null

    init {
        bluetoothConnector.addObserver(this)

        Scope.launch {
            bluetoothDeviceRepository.pairedDevices().distinctUntilChanged().cancellable().collect {
                bleManager.addPairedDeviceIds(it.toSet())
            }
        }

        Scope.launch(supervisor) {
            combine(
                bleManager.bluetoothActive,
                bleManager.uiOverride,
                bleManager.pairedDevices,
                bleManager.bgScanningActive,
                bleManager.devicesCurrentlyConnecting
            ) { btOn, uiOverride, paired, bgScanningActive, connectingDevices ->
                if (!btOn || connectingDevices.isNotEmpty() || !bgScanningActive && !uiOverride) ScanMode.Stopped
                else if (bgScanningActive && !uiOverride && paired.isNotEmpty()) ScanMode.Background
                else ScanMode.Foreground
            }
                .distinctUntilChanged()
                .collectLatest { mode ->
                    when (mode) {
                        ScanMode.Stopped -> stopPeriodicScan()
                        ScanMode.Foreground -> startPeriodicScan(scanDuration, scanInterval)
                        ScanMode.Background -> startPeriodicScan(
                            BACKGROUND_SCAN_DURATION,
                            BACKGROUND_SCAN_INTERVAL
                        )
                    }
                }
        }

        Scope.launch {
            bleManager.connectedDevices.collectLatest {
                observationFactory.updateObservationErrors()
            }
        }
    }

    fun observerDeviceAccessible(bleDevices: Set<String>): Boolean {
        if (bleDevices.anyNameIn(bleManager.pairedDevices.value)) {
            if (bleDevices.anyNameIn(bleManager.connectedDevices.value)) {
                disableBackgroundScanner()
                return true
            } else {
                enableBackgroundScanner()
            }
        } else {
            if (!bleManager.uiOverride.value && !ViewManager.bleViewActive.value) {
                ViewManager.showBLEView(true)
            }
        }
        return false
    }

    private fun enableBackgroundScanner() {
        bleManager.enableBgScanning()
    }

    private fun disableBackgroundScanner() {
        bleManager.disableBgScanning()
    }

    override fun viewIdentifier(): String {
        return NavigationRoute.BLUETOOTH_CONNECTION.viewIdentifier
    }

    override fun viewDidAppear() {
        bleManager.uiOverrides(true)
    }

    override fun viewDidDisappear() {
        bleManager.uiOverrides(false)
    }

    private fun startPeriodicScan(duration: Long, interval: Long) {
        if (periodicScanJob?.isActive == true || bleManager.scanning.value || bleManager.devicesCurrentlyConnecting.value.isNotEmpty()) {
            return
        }
        periodicScanJob = Scope.repeatedLaunch(interval, supervisor) {
            if (!bleManager.uiOverride.value) {
                while (!ViewManager.appInForeground.value) {
                    delay(BACKGROUND_SCAN_DURATION)
                }
                if (bleManager.bgScanningActive.value) {
                    delay(BACKGROUND_SCAN_DURATION)
                }
            }
            Napier.d { "Scanning for Bluetooth Devices..." }
            bluetoothConnector.scan()
            delay(duration)
            Napier.d { "Stopping BLE Scan" }
            bluetoothConnector.stopScanning()
        }.second
    }

    private fun stopPeriodicScan() {
        periodicScanJob?.cancel()
        periodicScanJob = null
        bluetoothConnector.stopScanning()
    }

    suspend fun connectToDevice(device: BluetoothDeviceEntity): Boolean {
        return withContext(Dispatchers.IO) {
            if (!bleManager.connectedDevices.value.contains(device)) {
                Napier.i(tag = "BluetoothController::connectToDevice") { "Connecting to $device" }
                bleManager.addConnectingDevices(setOf(device))
                return@withContext bluetoothConnector.connect(device) == null
            }
            return@withContext true
        }
    }

    fun unpairFromDevice(device: BluetoothDeviceEntity) {
        Napier.i(tag = "BluetoothController::disconnectFromDevice") { "Disconnecting from $device" }
        bluetoothConnector.disconnect(device)
        bluetoothDeviceRepository.unpairDevice(device)
        bleManager.removePairedDeviceIds(setOf(device))
    }

    override fun isConnectingToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        bleManager.addConnectingDevices(setOf(bluetoothDevice))
    }

    override fun didConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        bleManager.addConnectedDevices(setOf(bluetoothDevice))

        bluetoothDeviceRepository.storePairedDevice(bluetoothDevice)
    }

    override fun didDisconnectFromDevice(bluetoothDevice: BluetoothDeviceEntity) {
        Napier.i(tag = "BluetoothController::didDisconnectFromDevice") { "Disconnected from $bluetoothDevice" }
        bleManager.removeConnectedDevices(setOf(bluetoothDevice))
    }

    override fun didFailToConnectToDevice(bluetoothDevice: BluetoothDeviceEntity) {
        Napier.e(tag = "BluetoothController::didFailToConnectToDevice") { "Failed to connect to $bluetoothDevice" }
        bleManager.removeConnectingDevices(setOf(bluetoothDevice))
    }

    override fun didDiscoverDevice(device: BluetoothDeviceEntity) {
        if (!bleManager.connectedDevices.value.contains(device)) {
            Napier.i(tag = "BluetoothController::didDiscoverDevice") { "Discovered device: $device" }
            bleManager.addDiscoveredDevices(setOf(device))
            if (bleManager.pairedDevices.value.contains(device)) {
                Scope.launch {
                    connectToDevice(device)
                }
            }
        }
    }

    override fun removeDiscoveredDevice(device: BluetoothDeviceEntity) {
        Napier.i(tag = "BluetoothController::removeDiscoveredDevice") { "Removed discovered device: $device" }
        bleManager.removeDiscoveredDevices(setOf(device))
    }

    override fun resetAll() {
        Napier.i(tag = "BluetoothController::resetAll") { "Resetting Bluetooth data!" }
        disableBackgroundScanner()
        stopPeriodicScan()
        bleManager.uiOverrides(false)
        bleManager.clearDiscovered()
        bluetoothConnector.resetAll()
    }

    override fun close() {
        resetAll()
        supervisor.cancel()
    }

    companion object {
        // Energy-optimized scanning intervals
        private const val BACKGROUND_SCAN_DURATION = 1500L
        private const val BACKGROUND_SCAN_INTERVAL = 30000L
        private const val MAX_BACKGROUND_SCAN_INTERVAL = 300000L // Max 5 minutes between scans
    }
}

