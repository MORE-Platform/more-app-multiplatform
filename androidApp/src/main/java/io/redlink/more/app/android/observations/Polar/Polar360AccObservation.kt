package io.redlink.more.app.android.observations.Polar

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarSensorSetting
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
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
import io.redlink.more.observations.observationTypes.Polar360AccType
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

class Polar360AccObservation(repos: MainRepository) :
    Observation(repos, observationType = Polar360AccType(permissions)) {

    private val deviceIdentifier = setOf("Polar")
    private var accDisposable: Disposable? = null
    private var offlineRecordingDisposable: Disposable? = null
    private var deviceConnectionListener: Job? = null
    private var offlineMode = false

    override fun start(): Boolean {
        Napier.d(tag = "Polar360AccObservation::start") { "Starting Polar 360 ACC (offline=$offlineMode)..." }
        if (!observerAccessible()) {
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val device = Polar360Controller.findPolar360Device()
        if (device == null) {
            Napier.d(tag = "Polar360AccObservation::start") { "No Polar 360 device connected" }
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
                        deviceId, PolarBleApi.PolarDeviceDataType.ACC
                    )
                } else {
                    accDisposable = Polar360Controller.getPolarApi()
                        .requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
                        .subscribeOn(Schedulers.io())
                        .onErrorResumeNext { error: Throwable ->
                            Log.e(TAG, "ACC settings request failed: $error")
                            Single.just(
                                PolarSensorSetting(
                                    hashMapOf(
                                        PolarSensorSetting.SettingType.SAMPLE_RATE to 1,
                                        PolarSensorSetting.SettingType.RESOLUTION to 1
                                    )
                                )
                            )
                        }
                        .observeOn(Schedulers.io())
                        .toFlowable()
                        .flatMap { settings: PolarSensorSetting ->
                            Log.d(TAG, "Using ACC settings: $settings")
                            Polar360Controller.getPolarApi()
                                .startAccStreaming(deviceId, settings) as Flowable<PolarAccelerometerData>
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            { data: PolarAccelerometerData ->
                                if (data.samples.isNotEmpty()) {
                                    val sample = data.samples[0]
                                    Log.d(TAG, "ACC x=${sample.x} y=${sample.y} z=${sample.z}")
                                    storeData(accItem(x = sample.x, y = sample.y, z = sample.z, timestamp = sample.timeStamp))
                                }
                            },
                            { error ->
                                Log.e(TAG, "ACC stream failed: ${error.localizedMessage}")
                                accDisposable = null
                            }
                        )
                }
            },
            onError = { error ->
                Napier.e(tag = "Polar360AccObservation") { "Setup failed: ${error.message}" }
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
            Polar360Controller.stopOfflineRecording(PolarBleApi.PolarDeviceDataType.ACC)
                .subscribe(
                    { items ->
                        val processed = processAccSamples(items.filterIsInstance<PolarAccelerometerData.PolarAccelerometerDataSample>())
                        storeData(mapOf("polar360accdata" to processed), -1, onCompletion)
                    },
                    { error ->
                        Napier.e(tag = "Polar360AccObservation") { "Failed to process offline data: ${error.message}" }
                        onCompletion()
                    }
                )
        } else {
            accDisposable?.dispose()
            accDisposable = null
            onCompletion()
        }
    }

    data class accItem(val x: Int, val y: Int, val z: Int, val timestamp: Long)

    fun processAccSamples(samples: List<PolarAccelerometerData.PolarAccelerometerDataSample>?): List<accItem> {
        if (samples.isNullOrEmpty()) return emptyList()
        return samples.map { accItem(x = it.x, y = it.y, z = it.z, timestamp = it.timeStamp) }
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
                    pauseObservation(Polar360AccType(emptySet()))
                    Polar360Controller.onDeviceDisconnected()
                }
            }
        }.second
    }
}
