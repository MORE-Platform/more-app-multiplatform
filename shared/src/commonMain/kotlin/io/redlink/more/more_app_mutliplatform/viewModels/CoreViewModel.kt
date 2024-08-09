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
package io.redlink.more.more_app_mutliplatform.viewModels

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

abstract class CoreViewModel : Closeable {
    private val mutex = Mutex()
    private var viewJobs = mutableSetOf<String>()

    abstract fun viewDidAppear()

    open fun viewDidDisappear() {
        cancelScope()
    }

    fun launchScope(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val result = Scope.launch(coroutineContext, start, block)
        Scope.launch {
            mutex.withLock {
                viewJobs.add(result.first)
            }
        }
        result.second.invokeOnCompletion {
            Scope.launch {
                mutex.withLock {
                    viewJobs.remove(result.first)
                }
            }
        }
    }

    private fun cancelScope() {
        Scope.launch {
            mutex.withLock {
                val jobsToCancel = viewJobs.toSet()
                viewJobs.clear()
                Scope.cancel(jobsToCancel)
            }
        }
    }

    override fun close() {
        cancelScope()
    }
}