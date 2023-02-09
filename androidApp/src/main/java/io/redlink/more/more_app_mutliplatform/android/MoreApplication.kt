package io.redlink.more.more_app_mutliplatform.android

import android.app.Application
import android.content.Context


private const val TAG = "MoreApplication"

/**
 * Main Application class of the project.
 */
class MoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}