package io.redlink.more.app.android

import android.app.Application
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.redlink.more.more_app_mutliplatform.napierDebugBuild


private const val TAG = "MoreApplication"

/**
 * Main Application class of the project.
 */
class MoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        firebaseAnalytics = Firebase.analytics
        napierDebugBuild()

        appContext = this
    }

    companion object {
        var appContext: Context? = null
            private set

        var firebaseAnalytics: FirebaseAnalytics? = null
            private set

//        fun logEvent() {
//            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.)
//        }
    }
}