package io.redlink.more.more_app_mutliplatform.android.observations.HR

interface HeartRateListener {
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onHeartRateUpdate(hr: Int)
    fun onHeartRateReady()
}