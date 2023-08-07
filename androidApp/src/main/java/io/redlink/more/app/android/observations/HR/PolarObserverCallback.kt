package io.redlink.more.app.android.observations.HR


import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.model.PolarDeviceInfo
import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState
import java.util.UUID

class PolarObserverCallback : PolarBleApiCallback() {

    private var listeners: MutableSet<HeartRateListener> = mutableSetOf()

    var connectionListener: PolarConnectorListener? = null

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
        Napier.d("BLE power: $powered", tag = "PolarObserverCallback::blePowerStateChanged")
        connectionListener?.onPowerChange(if (powered) BluetoothState.ON else BluetoothState.OFF)
    }

    override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnected(polarDeviceInfo)
        Napier.d("CONNECTED: ${polarDeviceInfo.deviceId}", tag = "PolarObserverCallback::deviceConnected")
        connectionListener?.onDeviceConnected(polarDeviceInfo)
    }

    override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnecting(polarDeviceInfo)
        Napier.d("CONNECTING: ${polarDeviceInfo.deviceId}", tag = "PolarObserverCallback::deviceConnecting")
        connectionListener?.onDeviceConnecting(polarDeviceInfo)
    }

    override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceDisconnected(polarDeviceInfo)
        Napier.i("Device disconnected: ${polarDeviceInfo.name}", tag = "PolarObserverCallback::deviceDisconnected")
        connectionListener?.onDeviceDisconnected(polarDeviceInfo)
    }

    override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
        super.bleSdkFeatureReady(identifier, feature)
        Napier.i("SDK Feature ready: ${feature.name}, identifier: $identifier", tag = "PolarObserverCallback::bleSdkFeatureReady")
        connectionListener?.onPolarFeatureReady(feature)
    }

    override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
        super.disInformationReceived(identifier, uuid, value)
        Napier.i("Disinformation: $identifier, UUID: $uuid, Value: $value", tag = "PolarObserverCallback::disInformationReceived")
    }

    override fun batteryLevelReceived(identifier: String, level: Int) {
        super.batteryLevelReceived(identifier, level)
        Napier.i( "Battery Level Received: $level", tag = "PolarObserverCallback::batteryLevelReceived")
    }
}