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
package io.redlink.more.more_app_mutliplatform.util

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.extensions.repeatEveryFewSeconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

object Scope {
    private val mutex = Mutex()
    private val rootJob = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Napier.e(throwable = exception, message = "Caught $exception in CoroutineExceptionHandler")
    }
    private val scope = CoroutineScope(rootJob + Dispatchers.Default + exceptionHandler)
    private val jobs = mutableMapOf<String, Job>()

    fun launch(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = createUUID()
        val job = scope.launch(coroutineContext + exceptionHandler, start, block)
        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
                job.invokeOnCompletion {
                    scope.launch {
                        mutex.withLock {
                            try {
                                jobs.remove(uuid)
                            }  catch (e: Exception) {
                                if (e !is CancellationException) {
                                    Napier.e(tag = "Scope::launch::invokeOnCompletion") { e.stackTraceToString() }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair(uuid, job)
    }

    fun create(): Pair<String, Job> {
        val job = Job(rootJob)
        val uuid = createUUID()
        job.invokeOnCompletion {
            scope.launch {
                mutex.withLock {
                    try {
                        it?.let {
                            Napier.w(throwable = it) { "Coroutine with UUID: $uuid was completed or threw!" }
                        }
                        jobs.remove(uuid)
                    } catch (e: Exception) {
                        Napier.e { e.stackTraceToString() }
                    }
                }
            }
        }
        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
            }
        }
        return Pair(uuid, job)
    }

    fun isActive(uuid: String) = jobs[uuid]?.isActive ?: false

    fun repeatedLaunch(intervalMillis: Long, block: suspend CoroutineScope.() -> Unit): Pair<String, Job> {
        val uuid = createUUID()
        val job = scope.repeatEveryFewSeconds(intervalMillis, block)
        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
                job.invokeOnCompletion {
                    scope.launch {
                        mutex.withLock {
                            try {
                                it?.let {
                                    Napier.e(throwable = it) { "Coroutine with UUID: $uuid has thrown!" }
                                }
                                jobs.remove(uuid)
                            } catch (e: Exception) {
                                Napier.e { e.stackTraceToString() }
                            }
                        }
                    }
                }
            }
        }
        return Pair(uuid, job)
    }

    fun cancel(uuid: String) {
        scope.launch {
            mutex.withLock {
                jobs[uuid]?.cancel()
            }
        }
    }

    fun cancel(uuids: Collection<String>) {
        val set = uuids.toSet()
        scope.launch {
            mutex.withLock {
                val jobsToCancel = jobs.filter { it.key in set }.toList()
                try {
                    jobsToCancel.forEach { it.second.cancel() }
                } catch (exception: Exception) {
                    Napier.e { exception.stackTraceToString() }
                }
            }
        }
    }

    fun cancel() {
        scope.launch {
            mutex.withLock {
                rootJob.cancelChildren()
            }
        }
    }
}