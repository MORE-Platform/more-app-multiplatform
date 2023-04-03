package io.redlink.more.more_app_mutliplatform.android.observations.HR


import android.util.Log
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import java.util.UUID

private const val TAG = "PolarObserverCallback"

class PolarObserverCallback : PolarBleApiCallback() {
    private var listeners: MutableSet<HRListener> = mutableSetOf()

    fun addListener(listener: HRListener) {
        this.listeners.add(listener)
    }

    fun removeListener(listener: HRListener): Int {
        this.listeners.remove(listener)
        return this.listeners.size
    }

    private fun updateListeners(update: (HRListener) -> Unit) {
        listeners.forEach(update)
    }

    override fun blePowerStateChanged(powered: Boolean) {
        super.blePowerStateChanged(powered)
        Log.i(TAG, "Power State changed of connected device! Powered: $powered")
    }

    override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnected(polarDeviceInfo)
        Log.i(TAG, "Device connected: ${polarDeviceInfo.name}")
    }

    override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnecting(polarDeviceInfo)
        Log.i(TAG, "Device connecting: ${polarDeviceInfo.name}")
        updateListeners { it.onDeviceConnected() }
    }

    override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceDisconnected(polarDeviceInfo)
        Log.i(TAG, "Device disconnecting: ${polarDeviceInfo.name}")
        updateListeners { it.onDeviceDisconnected() }
    }

    override fun streamingFeaturesReady(
        identifier: String,
        features: MutableSet<PolarBleApi.DeviceStreamingFeature>,
    ) {
        super.streamingFeaturesReady(identifier, features)
        for (feature in features) {
            Log.d(TAG, "Streaming feature $feature is ready with id: $identifier")
        }
    }

    override fun sdkModeFeatureAvailable(identifier: String) {
        super.sdkModeFeatureAvailable(identifier)
    }

    override fun hrFeatureReady(identifier: String) {
        super.hrFeatureReady(identifier)
        Log.i(TAG, "HR Feature Ready: $identifier")
    }

    override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
        super.disInformationReceived(identifier, uuid, value)
        Log.i(TAG, "Disinformation: $identifier, UUID: $uuid, Value: $value")
    }

    override fun batteryLevelReceived(identifier: String, level: Int) {
        super.batteryLevelReceived(identifier, level)
        Log.i(TAG, "Battery Level Received: $level")
    }

    override fun hrNotificationReceived(identifier: String, data: PolarHrData) {
        super.hrNotificationReceived(identifier, data)
        updateListeners { it.onHeartRateUpdate(data.hr) }
    }

    override fun polarFtpFeatureReady(identifier: String) {
        super.polarFtpFeatureReady(identifier)
        Log.i(TAG, "PolarFTPFeature ready: $identifier")
    }
}