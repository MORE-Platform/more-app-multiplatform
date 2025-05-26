package io.redlink.more.app.android.activities.main

import android.app.Application
import io.redlink.more.app.android.observations.HealthKit.HealthConnectManager

class BaseApplication : Application() {
    val healthConnectManager by lazy {
         (this)
    }
}