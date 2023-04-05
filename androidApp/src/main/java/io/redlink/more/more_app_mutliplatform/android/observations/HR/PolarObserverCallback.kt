package io.redlink.more.more_app_mutliplatform.android.observations.HR


import android.util.Log
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.model.PolarDeviceInfo
import java.util.UUID

private const val TAG = "PolarObserverCallback"

class PolarObserverCallback : PolarBleApiCallback() {

    private var listeners: MutableSet<HeartRateListener> = mutableSetOf()

    fun addListener(listener: HeartRateListener) {
        this.listeners.add(listener)
    }

    fun removeListener(listener: HeartRateListener): Int {
        this.listeners.remove(listener)
        return this.listeners.size
    }

    private fun updateListeners(update: (HeartRateListener) -> Unit) {
        listeners.forEach(update)
    }

    override fun blePowerStateChanged(powered: Boolean) {
        super.blePowerStateChanged(powered)
        Log.d(TAG, "BLE power: $powered")
    }

    override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnected(polarDeviceInfo)
        Log.d(TAG, "CONNECTED: ${polarDeviceInfo.deviceId}")
    }

    override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnecting(polarDeviceInfo)
        Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
        updateListeners { it.onDeviceConnected() }
    }

    override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceDisconnected(polarDeviceInfo)
        Log.i(TAG, "Device disconnecting: ${polarDeviceInfo.name}")
        updateListeners { it.onDeviceDisconnected() }
    }

    override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
        super.bleSdkFeatureReady(identifier, feature)
        Log.i(TAG, "SDK Feature ready: ${feature.name}, identifier: $identifier")
    }

    override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
        super.disInformationReceived(identifier, uuid, value)
        Log.i(TAG, "Disinformation: $identifier, UUID: $uuid, Value: $value")
    }

    override fun batteryLevelReceived(identifier: String, level: Int) {
        super.batteryLevelReceived(identifier, level)
        Log.i(TAG, "Battery Level Received: $level")
    }
}