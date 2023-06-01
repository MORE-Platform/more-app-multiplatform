package io.redlink.more.app.android.observations.HR

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarDeviceInfo
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothState

interface PolarConnectorListener {
    fun onPolarFeatureReady(feature: PolarBleApi.PolarBleSdkFeature)
    fun onDeviceConnected(polarDeviceInfo: PolarDeviceInfo)
    fun onDeviceDisconnected(polarDeviceInfo: PolarDeviceInfo)

    fun onDeviceConnecting(polarDeviceInfo: PolarDeviceInfo)

    fun onPowerChange(bluetoothState: BluetoothState)
}