package io.redlink.more.app.android.observations.Polar

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import io.redlink.more.observations.observationTypes.Polar360HrType
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import io.redlink.more.services.bluetooth.polar.PolarStates
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

class Polar360HrObservation(repos: MainRepository) :
    Observation(repos, observationType = Polar360HrType(permissions)) {

    private val deviceIdentifier = setOf("Polar")
    private var hrDisposable: Disposable? = null
    private var offlineRecordingDisposable: Disposable? = null
    private var deviceConnectionListener: Job? = null
    private var offlineMode = false

    override fun start(): Boolean {
        Napier.d(tag = "Polar360HrObservation::start") { "Starting Polar 360 HR (offline=$offlineMode)..." }
        if (!observerAccessible()) {
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val device = Polar360Controller.findPolar360Device()
        if (device == null) {
            Napier.d(tag = "Polar360HrObservation::start") { "No Polar 360 device connected" }
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
                    offlineRecordingDisposable = Polar360Controller.startOfflineRecording(
                        deviceId, PolarBleApi.PolarDeviceDataType.PPI
                    )
                } else {
                    hrDisposable = Polar360Controller.getPolarApi()
                        .startHrStreaming(deviceId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            { polarData ->
                                if (polarData.samples.isNotEmpty()) {
                                    val sample = polarData.samples[0]
                                    Log.d(TAG, "HR: ${sample.hr}")
                                    storeData(hr_data(hr = sample.hr, ts = 0L))
                                }
                            },
                            { error ->
                                Napier.e(tag = "Polar360HrObservation", message = "HR stream error: ${error.message}")
                                pauseObservation(Polar360HrType(emptySet()))
                                showObservationErrorNotification(
                                    stringResource(R.string.observation_bluetooth_error),
                                    stringResource(R.string.observation_error)
                                )
                            }
                        )
                }
            },
            onError = { error ->
                Napier.e(tag = "Polar360HrObservation") { "Setup failed: ${error.message}" }
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
                        val processed = processHrSamples(items.filterIsInstance<PolarPpiData.PolarPpiSample>())
                        storeData(mapOf("polar36q0hrdata" to processed), -1,onCompletion)
                    },
                    { error ->
                        Napier.e(tag = "Polar360HrObservation") { "Failed to process offline data: ${error.message}" }
                        onCompletion()
                    }
                )
        } else {
            hrDisposable?.dispose()
            hrDisposable = null
            onCompletion()
        }
    }

    data class hr_data(val hr: Int, val ts: Long)

    fun processHrSamples(samples: List<PolarPpiData.PolarPpiSample>?): List<hr_data> {
        if (samples.isNullOrEmpty()) return emptyList()
        return samples.map { hr_data(hr = it.hr, ts = it.timeStamp.toLong()) }
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
        } else if (!PolarStates.hrFeatureReady.value) {
            errors.add("hr_unavailable")
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
                    pauseObservation(Polar360HrType(emptySet()))
                    Polar360Controller.onDeviceDisconnected()
                }
            }
        }.second
    }
}
