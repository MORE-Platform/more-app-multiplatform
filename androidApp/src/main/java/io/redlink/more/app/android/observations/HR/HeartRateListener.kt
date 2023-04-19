package io.redlink.more.app.android.observations.HR

interface HeartRateListener {
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onHeartRateUpdate(hr: Int)
    fun onHeartRateReady()
}