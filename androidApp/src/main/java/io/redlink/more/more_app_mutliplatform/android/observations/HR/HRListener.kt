package io.redlink.more.more_app_mutliplatform.android.observations.HR

interface HRListener {
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onHeartRateUpdate(hr: Int)
}