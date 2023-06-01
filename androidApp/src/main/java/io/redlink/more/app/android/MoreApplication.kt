package io.redlink.more.app.android

import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.redlink.more.app.android.observations.AndroidObservationDataManager
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.app.android.services.bluetooth.AndroidBluetoothConnector
import io.redlink.more.app.android.services.bluetooth.PolarConnector
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.napierDebugBuild
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository

/**
 * Main Application class of the project.
 */
class MoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            napierDebugBuild()
        }

        appContext = this

        shared = Shared(SharedPreferencesRepository(this))
        observationDataManager = AndroidObservationDataManager(this)
        polarConnector = PolarConnector(this)
        androidBluetoothConnector = AndroidBluetoothConnector(this)
        observationFactory = AndroidObservationFactory(this)
        observationFactory?.let {
            observationManager = ObservationManager(it)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        androidBluetoothConnector?.close()
    }

    companion object {
        var appContext: Context? = null
            private set

        var firebaseAnalytics: FirebaseAnalytics? = null
            private set

        var shared: Shared? = null
            private set

        var observationDataManager: ObservationDataManager? = null
            private set

        var observationFactory: ObservationFactory? = null
            private set

        var observationManager: ObservationManager? = null
            private set

        var polarConnector: PolarConnector? = null
            private set

        var androidBluetoothConnector: AndroidBluetoothConnector? = null
            private set

    }
}