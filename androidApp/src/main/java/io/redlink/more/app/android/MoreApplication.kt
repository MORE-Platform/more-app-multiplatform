package io.redlink.more.app.android

import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.AndroidObservationDataManager
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.app.android.services.LocalPushNotificationService
import io.redlink.more.app.android.services.bluetooth.PolarConnector
import io.redlink.more.app.android.util.AndroidLogHandler
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.napierDebugBuild
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.util.ElasticAntilog

/**
 * Main Application class of the project.
 */
class MoreApplication : Application(), DefaultLifecycleObserver {
    override fun onCreate() {
        super<Application>.onCreate()
        val workManager = WorkManager.getInstance(this)
        val logger = ElasticAntilog(AndroidLogHandler(workManager))
        napierDebugBuild(logger)
        appContext = this

        initShared(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        shared?.mainBluetoothConnector?.close()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Napier.d { "App is in the foreground..." }
        shared?.appInForeground(true)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Napier.d { "App is in the background..." }
        shared?.appInForeground(false)
    }

    companion object {
        var appContext: Context? = null
            private set

        var firebaseAnalytics: FirebaseAnalytics? = null
            private set

        var shared: Shared? = null
            private set

        var polarConnector: PolarConnector? = null
            private set


        fun initShared(context: Context) {
            if (shared == null) {
                polarConnector = PolarConnector(context)
                val androidBluetoothConnector = polarConnector!!
                val dataManager = AndroidObservationDataManager(context)
                shared = Shared(
                    null,
                    LocalPushNotificationService(context),
                    SharedPreferencesRepository(context),
                    dataManager,
                    androidBluetoothConnector,
                    AndroidObservationFactory(context, dataManager),
                    AndroidDataRecorder()
                )
            }
        }

    }
}