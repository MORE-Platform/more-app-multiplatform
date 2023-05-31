package io.redlink.more.more_app_mutliplatform.util

import io.github.aakira.napier.Napier
import io.realm.kotlin.internal.platform.freeze
import io.redlink.more.more_app_mutliplatform.extensions.repeatEveryFewSeconds
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
    private val scope = CoroutineScope(rootJob + Dispatchers.Default)
    private val jobs = mutableMapOf<String, Job>()

    fun launch(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = createUUID()
        val job = scope.launch(coroutineContext, start, block).freeze()
        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
                job.invokeOnCompletion {
                    Napier.d { "Job completed. Removing from list..." }
                    jobs.remove(uuid)
                }
            }
        }
        return Pair(uuid, job)
    }

    fun create(): Pair<String, Job> {
        val job = Job(rootJob)
        val uuid = createUUID()
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
        val job = scope.repeatEveryFewSeconds(intervalMillis, block).freeze()
        scope.launch {
            mutex.withLock {
                jobs[uuid] = job
                job.invokeOnCompletion {
                    Napier.d { "Job completed. Removing from list..." }
                    jobs.remove(uuid)
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
        val jobsToCancel = jobs.filter { it.key in set }.toList()
        scope.launch {
            mutex.withLock {
                jobsToCancel.forEach { it.second.cancel() }
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