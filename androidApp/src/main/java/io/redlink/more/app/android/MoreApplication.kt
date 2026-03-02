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
package io.redlink.more.app.android

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.aakira.napier.Napier
import io.redlink.more.Shared
import io.redlink.more.app.android.extensions.applicationId
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.AndroidObservationDataManager
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.app.android.services.LocalPushNotificationService
import io.redlink.more.app.android.services.bluetooth.PolarConnector
import io.redlink.more.app.android.util.logging.FirebaseCrashlyticsAntilog
import io.redlink.more.database.AppDatabase
import io.redlink.more.database.getDatabaseBuilder
import io.redlink.more.database.getRoomDatabase
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.models.NotificationTextLocalization
import io.redlink.more.napierDebugBuild
import io.redlink.more.services.store.SharedPreferencesRepository

/**
 * Main Application class of the project.
 */
class MoreApplication : Application(), DefaultLifecycleObserver {
    override fun onCreate() {
        super<Application>.onCreate()
        napierDebugBuild(FirebaseCrashlyticsAntilog())
        napierDebugBuild()
        appContext = this
        packagePath = this.packageName
        appName = this.getString(R.string.app_name)
        DEFAULT_CHANNEL_ID = packagePath + appName!!.lowercase() + ".urgent"
        NotificationTextLocalization.init(this)

        initShared(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onTerminate() {
        shared?.bluetoothController?.close()
        super.onTerminate()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Napier.i { "App is in the foreground..." }
        shared?.updateData(true)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Napier.i { "App is in the background..." }
        shared?.updateData(false)
    }

    companion object {
        var appContext: Context? = null
            private set

        var appName: String? = null
            private set

        var packagePath: String? = null
            private set

        var DEFAULT_CHANNEL_ID: String? = null
            private set

        var firebaseAnalytics: FirebaseAnalytics? = null
            private set

        var shared: Shared? = null
            private set

        var polarConnector: PolarConnector? = null
            private set

        val openSettings = mutableStateOf(false)

        fun initShared(context: Context) {
            if (shared == null) {
                polarConnector = PolarConnector(context)
                val androidBluetoothConnector = polarConnector!!
                val database: AppDatabase = getRoomDatabase(getDatabaseBuilder(context))
                val repositories = MainRepository(database)
                val dataManager = AndroidObservationDataManager(context, repositories)
                shared = Shared(
                    LocalPushNotificationService(context),
                    repositories,
                    SharedPreferencesRepository(context),
                    dataManager,
                    androidBluetoothConnector,
                    AndroidObservationFactory(context, dataManager, repositories),
                    AndroidDataRecorder()
                )
                shared?.let { shared ->
                    shared.deeplinkManager.setProtocol(Shared.PROTOCOL.toString(context))
                    shared.deeplinkManager.setHost(applicationId) // applicationId is needed instead of the shared HOST, as this is necessary for the NavController in Android
                }
            }
        }
    }
}