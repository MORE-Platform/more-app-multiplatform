/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.scopes

import io.github.aakira.napier.Napier
import io.redlink.more.extensions.repeatEveryFewSeconds
import io.redlink.more.util.createUUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

object Scope : MoreScope {
    private val mutex = Mutex()
    private val rootJob = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Napier.e(throwable = exception, message = "Caught $exception in CoroutineExceptionHandler")
    }
    private val scope = CoroutineScope(rootJob + AppDispatchers.default + exceptionHandler)
    override val coroutineContext: CoroutineContext = scope.coroutineContext
    private val jobs = mutableMapOf<String, Job>()

    override fun launch(
        coroutineContext: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = createUUID()
        val job = scope.launch(coroutineContext + exceptionHandler, start, block)

        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
            }
        }

        job.invokeOnCompletion { cause ->
            scope.launch {
                mutex.withLock {
                    try {
                        jobs.remove(uuid)
                        cause?.let {
                            if (it !is CancellationException) {
                                Napier.w(throwable = it) { "Job with UUID: $uuid completed with exception" }
                            }
                        }
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            Napier.e(tag = "Scope::launch::cleanup") { e.stackTraceToString() }
                        }
                    }
                }
            }
        }

        return Pair(uuid, job)
    }

    fun create(): Pair<String, Job> {
        val uuid = createUUID()
        val job = Job(rootJob)

        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
            }
        }

        job.invokeOnCompletion { cause ->
            scope.launch {
                mutex.withLock {
                    try {
                        jobs.remove(uuid)
                        cause?.let {
                            if (it !is CancellationException) {
                                Napier.w(throwable = it) { "Job with UUID: $uuid was completed with exception" }
                            }
                        }
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            Napier.e(tag = "Scope::create::cleanup") { e.stackTraceToString() }
                        }
                    }
                }
            }
        }

        return Pair(uuid, job)
    }

    fun isActive(uuid: String) = jobs[uuid]?.isActive ?: false

    override fun repeatedLaunch(
        intervalMillis: Long,
        coroutineContext: CoroutineContext,
        initalDelay: Long,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = createUUID()
        val job = scope.repeatEveryFewSeconds(intervalMillis, initalDelay, coroutineContext, block)

        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
            }
        }

        job.invokeOnCompletion { cause ->
            scope.launch {
                mutex.withLock {
                    try {
                        jobs.remove(uuid)
                        cause?.let {
                            if (it !is CancellationException) {
                                Napier.w(throwable = it) { "Repeated job with UUID: $uuid completed with exception" }
                            }
                        }
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            Napier.e(tag = "Scope::repeatedLaunch::cleanup") { e.stackTraceToString() }
                        }
                    }
                }
            }
        }

        return Pair(uuid, job)
    }

    override fun cancel(uuid: String) {
        scope.launch {
            mutex.withLock {
                jobs[uuid]?.cancel()
            }
        }
    }

    override fun cancel(uuids: Collection<String>) {
        if (uuids.isEmpty()) return

        scope.launch {
            mutex.withLock {
                try {
                    uuids.forEach { uuid ->
                        jobs[uuid]?.cancel()
                    }
                } catch (exception: Exception) {
                    if (exception !is CancellationException) {
                        Napier.e(tag = "Scope::cancel::batch") { exception.stackTraceToString() }
                    }
                }
            }
        }
    }

    override fun cancel() {
        scope.launch {
            mutex.withLock {
                try {
                    rootJob.cancelChildren()
                } catch (exception: Exception) {
                    if (exception !is CancellationException) {
                        Napier.e(tag = "Scope::cancel::all") { exception.stackTraceToString() }
                    }
                }
            }
        }
    }
}