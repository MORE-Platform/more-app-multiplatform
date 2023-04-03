package io.redlink.more.more_app_mutliplatform.android.observations.HR

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.more_app_mutliplatform.android.observations.external_device.ExternalDeviceInterface

import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationTypes.PolarVerityHeartRateType


private const val TAG = "HearRateObservation"
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


class HeartRateObservation(
    context: Context
) : Observation(observationTypeImpl = PolarVerityHeartRateType(permissions)), HRListener, ExternalDeviceInterface<PolarDeviceInfo> {
    private val polarApi: PolarBleApi =
        PolarBleApiDefaultImpl.defaultImplementation(context, PolarBleApi.ALL_FEATURES)
    private val polarCallback = PolarObserverCallback()
    private val hasPermission = this.hasPermissions(context)
    private var searchSubscriber: Disposable? = null

    override fun onDeviceConnected() {
        Log.d(TAG, "Device connected. Listening to HR")
    }

    override fun onDeviceDisconnected() {
        stopListenToHR(this)
    }

    override fun onHeartRateUpdate(hr: Int) {
        Log.d(TAG, "HR Updated: $hr BPM")
        if (hr > 0) {
            storeData(hr)
        }
    }

    override fun start(): Boolean {
        println("Observation HeartRate started!")
        if (this.activate()) {
            val listener = this
            this.listeningToHR(listener)
            return true
        }
        return false
    }

    override fun stop() {
        stopListenToHR(this)
    }

    override fun observerAccessible(): Boolean {
        return hasPermission
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }

    fun listeningToHR(listener: HRListener) = polarCallback.addListener(listener)

    fun stopListenToHR(listener: HRListener) {
        if (polarCallback.removeListener(listener) == 0) {
            polarApi.cleanup()
        }
    }

    fun getPermission(): Set<String> = permissions

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

    private fun activate(): Boolean {
        if (hasPermission) {
            setup()
            return true
        }
//        sendError(title = context.getString(R.string.more_permission_missing_error_title),
//            message = context.getString(R.string.more_permission_missing_error_message),
//            true)
        stop()
        return false
    }

    private fun setup(): Boolean {
        println("Setting up a device!")
        polarApi.setApiCallback(polarCallback)
        return searchForDevice("Sense")
    }

    override var sensor: PolarDeviceInfo?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun searchForDevice(name: String): Boolean {
        return try {
            getDeviceWithName(name)
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())

            stopScanning()
            sensor?.let { disconnectFromDevice(it) }
            false
        }
    }

    override fun getDeviceWithName(name: String): Boolean {
        println("Searching for a device!")
        searchSubscriber = polarApi.searchForDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
//        searchSubscriber = polarApi.searchForDevice().subscribe {
            Log.i(TAG, "Device found: ${it.name}")
            if (it.name.contains(name)) {
                sensor = it
            }
            else {
//                Napier.e(title = context.getString(R.string.more_polar_observer_connect_error_title), message = context.getString(R.string.more_polar_observer_connect_error_message))
//                PushNotificationService.sendNotification(context, title = context.getString(R.string.more_polar_observer_connect_error_title), message = context.getString(
//                    R.string.more_polar_observer_connect_error_message))
                println("Connection Error!")
            }
        }
        return true
    }

    override fun connectToDevice(device: PolarDeviceInfo): Boolean = try {
        polarApi.setAutomaticReconnection(true)
        polarApi.connectToDevice(device.deviceId)
        true
    } catch (e: Exception) {
        Log.e(TAG, e.stackTraceToString())
        false
    }

    override fun stopScanning() {
        searchSubscriber?.dispose()
    }

    override fun disconnectFromDevice(device: PolarDeviceInfo) {
        polarApi.setAutomaticReconnection(false)
        polarApi.disconnectFromDevice(device.deviceId)
        polarApi.shutDown()
        polarApi.cleanup()
    }

}