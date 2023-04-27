package io.redlink.more.app.android.observations.HR

import com.polar.sdk.api.model.PolarDeviceInfo

interface HeartRateListener {
    fun onHeartRateUpdate(hr: Int)
    fun onHeartRateReady()
}