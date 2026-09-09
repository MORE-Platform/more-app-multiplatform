/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.observations.PolarObservations

import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHealthThermometerData
import io.github.aakira.napier.Napier
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import java.util.UUID

class PolarObserverCallback : PolarBleApiCallback() {
    var connectionListener: PolarConnectorListener? = null

    override fun blePowerStateChanged(powered: Boolean) {
        super.blePowerStateChanged(powered)
        Napier.d("BLE power: $powered", tag = "PolarObserverCallback::blePowerStateChanged")
        BluetoothStateManagement.setBluetoothState(powered)
    }

    override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnected(polarDeviceInfo)
        Napier.d(
            "CONNECTED: ${polarDeviceInfo.deviceId}",
            tag = "PolarObserverCallback::deviceConnected"
        )
        connectionListener?.onDeviceConnected(polarDeviceInfo)
    }

    override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceConnecting(polarDeviceInfo)
        Napier.d(
            "CONNECTING: ${polarDeviceInfo.deviceId}",
            tag = "PolarObserverCallback::deviceConnecting"
        )
        connectionListener?.onDeviceConnecting(polarDeviceInfo)
    }

    override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
        super.deviceDisconnected(polarDeviceInfo)
        Napier.i(
            "Device disconnected: ${polarDeviceInfo.name}",
            tag = "PolarObserverCallback::deviceDisconnected"
        )
        connectionListener?.onDeviceDisconnected(polarDeviceInfo)
    }

    override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
        super.bleSdkFeatureReady(identifier, feature)
        Napier.i(
            "SDK Feature ready: ${feature.name}, identifier: $identifier",
            tag = "PolarObserverCallback::bleSdkFeatureReady"
        )
        connectionListener?.onPolarFeatureReady(feature)
    }

    override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
        super.disInformationReceived(identifier, uuid, value)
        Napier.i(
            "Disinformation: $identifier, UUID: $uuid, Value: $value",
            tag = "PolarObserverCallback::disInformationReceived"
        )
    }

    override fun htsNotificationReceived(
        identifier: String,
        data: PolarHealthThermometerData
    ) {
        Napier.i(">$identifier, $data", tag = "PolarObserverCallback::htsNotificationReceived")
    }

    override fun disInformationReceived(identifier: String, disInfo: DisInfo) {
        Napier.i(
            "Disinformation: $identifier, DisInfo: $disInfo",
            tag = "PolarObserverCallback::disInformationReceived"
        )
    }

    override fun batteryLevelReceived(identifier: String, level: Int) {
        super.batteryLevelReceived(identifier, level)
        Napier.i(
            "Battery Level Received: $level",
            tag = "PolarObserverCallback::batteryLevelReceived"
        )
    }
}