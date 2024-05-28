package io.redlink.more.more_app_mutliplatform.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

object StudyScope {
    private val mutex = Mutex()
    private val jobs = mutableSetOf<String>()

    fun launch(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val result = Scope.launch(coroutineContext, start, block)
        Scope.launch {
            mutex.withLock {
                jobs.add(result.first)

            }
        }
        result.second.invokeOnCompletion {
            Scope.launch {
                mutex.withLock {
                    jobs.remove(result.first)
                }
            }
        }
        return result
    }

    fun repeatedLaunch(
        intervalMillis: Long,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val result = Scope.repeatedLaunch(intervalMillis, block)
        Scope.launch {
            mutex.withLock {
                jobs.add(result.first)
            }
        }
        result.second.invokeOnCompletion {
            Scope.launch {
                mutex.withLock {
                    jobs.remove(result.first)
                }
            }
        }
        return result
    }

    fun cancel(uuid: String) {
        Scope.cancel(uuid)
    }

    fun cancel(uuids: Collection<String>) {
        Scope.cancel(uuids)
    }

    fun cancel() {
        Scope.cancel(this.jobs)
    }
}