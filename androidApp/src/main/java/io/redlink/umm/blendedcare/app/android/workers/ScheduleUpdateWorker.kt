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
package io.redlink.umm.blendedcare.app.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.participant.Shared
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleUpdateWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    private val shared: Shared

    init {
        if (BlendedCareApplication.shared == null) {
            BlendedCareApplication.initShared(applicationContext)
        }
        shared = BlendedCareApplication.shared!!
    }

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        Napier.i { "Running $WORKER_TAG! Updating Schedule..." }
        shared.repositories.schedule.updateTaskStates(
            shared.observationFactory,
            shared.dataRecorder
        )
        return@withContext Result.success()
    }

    companion object {
        const val WORKER_TAG = "ScheduleUpdateWorker"
    }
}