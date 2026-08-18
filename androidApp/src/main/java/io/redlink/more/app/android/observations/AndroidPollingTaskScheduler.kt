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
package io.redlink.more.app.android.observations

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.app.android.workers.PollingWorker
import io.redlink.more.observations.polling.PollingTaskScheduler
import java.util.concurrent.TimeUnit

/**
 * Schedules the single, shared [PollingWorker] as a unique periodic WorkManager request - see
 * [io.redlink.more.observations.polling.PollingObservationRegistry], which is the only caller and
 * already avoids resubmitting an unchanged request.
 */
class AndroidPollingTaskScheduler(private val context: Context) : PollingTaskScheduler {

    override fun schedule(intervalMillis: Long) {
        val intervalMinutes = (intervalMillis / 60_000L).coerceAtLeast(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS / 60_000L)
        val request = PeriodicWorkRequestBuilder<PollingWorker>(intervalMinutes, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PollingWorker.WORKER_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(PollingWorker.WORKER_TAG)
    }
}
