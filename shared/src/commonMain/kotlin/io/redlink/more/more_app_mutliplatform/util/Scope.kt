package io.redlink.more.more_app_mutliplatform.util

import io.redlink.more.more_app_mutliplatform.extensions.repeatEveryFewSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object Scope {
    private val rootJob = SupervisorJob()
    private val scope = CoroutineScope(rootJob + Dispatchers.Default)
    private val jobs = mutableMapOf<String, Job>()

    fun launch(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val uuid = createUUID()
        val job = scope.launch(coroutineContext, start, block)
        job.invokeOnCompletion {
            jobs.remove(uuid)
        }
        jobs[uuid] = job
        return Pair(uuid, job)
    }

    fun create(): Pair<String, Job> {
        val job = Job(rootJob)
        val uuid = createUUID()
        jobs[uuid] = job
        return Pair(uuid, job)
    }

    fun isActive(uuid: String) = jobs[uuid]?.isActive ?: false

    fun repeatedLaunch(intervalMillis: Long, block: suspend CoroutineScope.() -> Unit): Pair<String, Job> {
        val uuid = createUUID()
        val job = scope.repeatEveryFewSeconds(intervalMillis, block)
        job.invokeOnCompletion {
            jobs.remove(uuid)
        }
        jobs[uuid] = job
        return Pair(uuid, job)
    }

    fun cancel(uuid: String) {
        jobs[uuid]?.cancel()
    }

    fun cancel(uuids: Collection<String>) {
        val set = uuids.toSet()
        jobs.filter { it.key in set }.forEach { it.value.cancel() }
    }

    fun cancel() {
        rootJob.cancelChildren()
    }
}