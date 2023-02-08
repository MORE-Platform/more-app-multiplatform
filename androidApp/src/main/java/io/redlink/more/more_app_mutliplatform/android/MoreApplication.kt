package io.redlink.more.more_app_mutliplatform.android

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


private const val TAG = "MoreApplication"

/**
 * Main Application class of the project.
 */
@HiltAndroidApp
class MoreApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        context = this
    }
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Overrides the default WorkerFactory with a custom WorkerFactory.
     */
    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    companion object {
        var context: Context? = null
            private set
    }
}