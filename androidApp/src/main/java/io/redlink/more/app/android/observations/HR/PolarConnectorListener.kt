package io.redlink.more.app.android.observations.HR

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarDeviceInfo

interface PolarConnectorListener {
    fun onPolarFeatureReady(feature: PolarBleApi.PolarBleSdkFeature)
    fun onDeviceConnected(polarDeviceInfo: PolarDeviceInfo)
    fun onDeviceDisconnected(polarDeviceInfo: PolarDeviceInfo)
}