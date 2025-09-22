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
    private val studyJobs = mutableSetOf<String>()

    fun launch(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
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

    fun repeatedLaunch(
        intervalMillis: Long,
        coroutineContext: CoroutineContext = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job> {
        val result = Scope.repeatedLaunch(intervalMillis, coroutineContext, block)

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

    fun cancel(uuid: String) {
        Scope.cancel(uuid)
    }

    fun cancel(uuids: Collection<String>) {
        Scope.cancel(uuids)
    }

    fun cancel() {
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