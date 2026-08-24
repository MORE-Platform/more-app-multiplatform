/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.scopes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

object StudyScope : StudyMoreScope {
    private val mutex = Mutex()
    private val studyJobs = mutableSetOf<String>()
    override val coroutineContext: CoroutineContext = Scope.coroutineContext

    override fun launch(
        coroutineContext: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val result = Scope.launch(coroutineContext, start, block)

        Scope.launch {
            mutex.withLock {
                studyJobs.add(result.first)
            }
        }

        result.second.invokeOnCompletion {
            Scope.launch {
                mutex.withLock {
                    studyJobs.remove(result.first)
                }
            }
        }

        return result
    }

    override fun repeatedLaunch(
        intervalMillis: Long,
        coroutineContext: CoroutineContext,
        initalDelay: Long,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val result = Scope.repeatedLaunch(intervalMillis, coroutineContext, initalDelay, block)

        Scope.launch {
            mutex.withLock {
                studyJobs.add(result.first)
            }
        }

        result.second.invokeOnCompletion {
            Scope.launch {
                mutex.withLock {
                    studyJobs.remove(result.first)
                }
            }
        }

        return result
    }

    override fun cancel(uuid: String) {
        Scope.cancel(uuid)
    }

    override fun cancel(uuids: Collection<String>) {
        Scope.cancel(uuids)
    }

    override fun cancel() {
        val jobsToCancel = mutex.tryLock().let { acquired ->
            if (acquired) {
                try {
                    studyJobs.toList()
                } finally {
                    mutex.unlock()
                }
            } else {
                Scope.launch {
                    mutex.withLock {
                        val snapshot = studyJobs.toList()
                        if (snapshot.isNotEmpty()) {
                            Scope.cancel(snapshot)
                        }
                    }
                }
                return
            }
        }

        if (jobsToCancel.isNotEmpty()) {
            Scope.cancel(jobsToCancel)
        }
    }
}