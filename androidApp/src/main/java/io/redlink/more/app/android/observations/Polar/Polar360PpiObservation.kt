package io.redlink.more.app.android.observations.Polar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarPpiData
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.observations.pauseObservation
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.anyNameIn
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.Polar360PpiType
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import kotlinx.coroutines.Job

private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (Build.VERSION.SDK_INT >= 34) {
            setOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
            )
        } else {
            setOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    } else {
        setOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

class Polar360PpiObservation(repos: MainRepository) :
    Observation(repos, observationType = Polar360PpiType(permissions)) {

    private val deviceIdentifier = setOf("Polar")
    private var ppiDisposable: Disposable? = null
    private var offlineRecordingDisposable: Disposable? = null
    private var deviceConnectionListener: Job? = null
    private var offlineMode = false

    override fun start(): Boolean {
        Napier.d(tag = "Polar360PpiObservation::start") { "Starting Polar 360 PPI (offline=$offlineMode)..." }
        if (!observerAccessible()) {
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val device = Polar360Controller.findPolar360Device()
        if (device == null) {
            Napier.d(tag = "Polar360PpiObservation::start") { "No Polar 360 device connected" }
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val deviceId = device.deviceId!!
        Polar360Controller.ensureReady(deviceId, offlineMode = offlineMode,
            onReady = {
                if (offlineMode) {
                    Polar360Controller.stopOfflineRecording(PolarBleApi.PolarDeviceDataType.PPI)
                        .subscribe(
                            { items ->
                                val processed = processPpiSamples(items.filterIsInstance<PolarPpiData.PolarPpiSample>())
                                if (processed.isNotEmpty()) {
                                    storeData(mapOf("polar360ppidata" to processed), -1) {}
                                }
                                offlineRecordingDisposable = Polar360Controller.startOfflineRecording(
                                    deviceId, PolarBleApi.PolarDeviceDataType.PPI
                                )
                            },
                            { error ->
                                Napier.e(tag = "Polar360PpiObservation") { "Failed to fetch pending offline data: ${error.message}" }
                                offlineRecordingDisposable = Polar360Controller.startOfflineRecording(
                                    deviceId, PolarBleApi.PolarDeviceDataType.PPI
                                )
                            }
                        )
                } else {
                    ppiDisposable = Polar360Controller.getPolarApi()
                        .startPpiStreaming(deviceId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            { data: PolarPpiData ->
                                if (data.samples.isNotEmpty()) {
                                    val processed = processPpiSamples(data.samples)
                                    processed.forEach { storeData(it) }
                                }
                            },
                            { error ->
                                Napier.e(tag = "Polar360PpiObservation") { "PPI stream failed: ${error.message}" }
                                ppiDisposable = null
                            }
                        )
                }
            },
            onError = { error ->
                Napier.e(tag = "Polar360PpiObservation") { "Setup failed: ${error.message}" }
                showObservationErrorNotification(
                    stringResource(R.string.observation_cannot_start),
                    stringResource(R.string.observation_error)
                )
            }
        )

        deviceConnectionListener = listenToDeviceConnection()
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        deviceConnectionListener?.cancel()
        deviceConnectionListener = null
        if (offlineMode) {
            offlineRecordingDisposable?.dispose()
            offlineRecordingDisposable = null
            Polar360Controller.stopOfflineRecording(PolarBleApi.PolarDeviceDataType.PPI)
                .subscribe(
                    { items ->
                        val processed = processPpiSamples(items.filterIsInstance<PolarPpiData.PolarPpiSample>())
                        storeData(mapOf("polar360ppidata" to processed), -1, onCompletion)
                    },
                    { error ->
                        Napier.e(tag = "Polar360PpiObservation") { "Failed to process offline data: ${error.message}" }
                        onCompletion()
                    }
                )
        } else {
            ppiDisposable?.dispose()
            ppiDisposable = null
            onCompletion()
        }
    }

    data class ppi_data(val hr: Int, val timestamp: Long, val ppiInMs: Int, val ppiErrorEstimate: Int)

    fun processPpiSamples(samples: List<PolarPpiData.PolarPpiSample>?): List<ppi_data> {
        if (samples.isNullOrEmpty()) return emptyList()
        return samples.map {
            ppi_data(hr = it.hr, timestamp = it.timeStamp.toLong(), ppiInMs = it.ppi, ppiErrorEstimate = it.errorEstimate)
        }
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (!hasPermissions(MoreApplication.appContext!!)) {
            errors.add("error_access_bluetooth")
            showPermissionAlertDialog()
        }
        if (!BluetoothStateListener.bluetoothEnabled.value) {
            errors.add("bluetooth_disabled")
        }
        if (!MoreApplication.shared!!.bluetoothController.observerDeviceAccessible(deviceIdentifier)) {
            errors.add("device_not_connected")
            errors.add(ERROR_DEVICE_NOT_CONNECTED)
        }
        return errors
    }

    override fun bleDevicesNeeded(): Set<String> = deviceIdentifier

    override fun ableToAutomaticallyStart(): Boolean = observerAccessible()

    override fun applyObservationConfig(settings: Map<String, Any>) {
        offlineMode = Polar360Controller.isOfflineRecordingMode(settings)
    }

    private fun hasPermissions(context: Context): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_DENIED
        }
    }

    private fun listenToDeviceConnection(): Job {
        return Scope.launch {
            BluetoothStateManagement.connectedDevices.collect { devices ->
                if (!deviceIdentifier.anyNameIn(devices)) {
                    pauseObservation(Polar360PpiType(emptySet()))
                    Polar360Controller.onDeviceDisconnected()
                }
            }
        }.second
    }
}
