package io.redlink.more.more_app_mutliplatform.android.observations.HR

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.PolarVerityHeartRateType

private const val TAG = "PolarHearRateObservation"
private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        setOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
    }

class PolarHeartRateObservation(context: Context) : Observation(observationType = PolarVerityHeartRateType(permissions)) {

    private val api: PolarBleApi = PolarBleApiDefaultImpl.defaultImplementation(
        context,
        setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
            PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
        )
    )
    private val polarCallback = PolarObserverCallback()
    private val hasPermission = this.hasPermissions(context)

    private var deviceId = ""

    private lateinit var broadcastDisposable: Disposable
    private var scanDisposable: Disposable? = null
    private var autoConnectDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null

    private var deviceConnected = false

    override fun start(): Boolean {
        Log.d(TAG, "version: " + PolarBleApiDefaultImpl.versionInfo())

        api.setPolarFilter(false)
        api.setApiCallback(polarCallback)
        return autoConnect()
    }

    fun broadcast() {
        if (!this::broadcastDisposable.isInitialized || broadcastDisposable.isDisposed) {

            broadcastDisposable = api.startListenForPolarHrBroadcasts(null)
                .subscribe(
                    { polarBroadcastData: PolarHrBroadcastData ->
                        Log.d(TAG, "HR BROADCAST ${polarBroadcastData.polarDeviceInfo.deviceId} HR: ${polarBroadcastData.hr} batt: ${polarBroadcastData.batteryStatus}")
                    },
                    { error: Throwable -> Log.e(TAG, "Broadcast listener failed. Reason $error")
                    },
                    { Log.d(TAG, "complete") }
                )
        } else {
            broadcastDisposable.dispose()
        }
    }

    fun connectDevice(): Boolean {
        try {
            if (deviceConnected) {
                api.disconnectFromDevice(deviceId)
            } else {
                api.connectToDevice(deviceId)
                return true
            }
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            val attempt = if (deviceConnected) {
                "disconnect"
            } else {
                "connect"
            }
            Log.e(TAG, "Failed to $attempt. Reason $polarInvalidArgument ")
        }
        return true
    }

    private fun autoConnect(): Boolean {
        var success = false
        if (autoConnectDisposable != null) {
            autoConnectDisposable?.dispose()
            return true
        }
        autoConnectDisposable = api.autoConnectToDevice(-60, null, null)
            .subscribe(
                {
                    Log.d(TAG, "auto connect search complete")
                    startHR()
                    success = true
                },
                { throwable: Throwable -> Log.e(TAG, "" + throwable.toString()) }
            )
        return success
    }

    fun scanForDevices(): Boolean {
        val isDisposed = scanDisposable?.isDisposed ?: true
        if (isDisposed) {
            scanDisposable = api.searchForDevice()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarDeviceInfo: PolarDeviceInfo ->
                        Log.d(
                            TAG,
                            "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable
                        )
                        this.deviceId = polarDeviceInfo.deviceId
                    },
                    { error: Throwable ->
                        Log.e(TAG, "Device scan failed. Reason $error")
                    },
                    {
                        Log.d(TAG, "complete")
                    }
                )
            return true
        } else {
            scanDisposable?.dispose()
            return false
        }
    }

    fun startHR() {
        val isDisposed = hrDisposable?.isDisposed ?: true
        if (isDisposed) {
            hrDisposable = api.startHrStreaming(deviceId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            Log.d(
                                TAG,
                                "HR     bpm: ${sample.hr} rrs: ${sample.rrsMs} rrAvailable: ${sample.rrAvailable} contactStatus: ${sample.contactStatus} contactStatusSupported: ${sample.contactStatusSupported}"

                            )
                            Log.d(TAG, "HR Updated: ${sample.hr} BPM")
                            if (sample.hr > 0) {
                                storeData(sample.hr)
                            }
                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "HR stream failed. Reason $error")
                    },
                    { Log.d(TAG, "HR stream complete") }
                )
        } else {
            // NOTE dispose will stop streaming if it is "running"
            hrDisposable?.dispose()
        }
    }

    override fun stop(onCompletion: () -> Unit) {
        api.shutDown()
    }

    override fun observerAccessible(): Boolean {
        return hasPermission
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }

    private fun getPermission(): Set<String> = permissions

    private fun hasPermissions(context: Context): Boolean {
        getPermission().forEach { permission ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }
}