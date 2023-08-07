package io.redlink.more.more_app_mutliplatform.viewModels

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

abstract class CoreViewModel : Closeable {
    private val mutex = Mutex()
    private var viewJobs = mutableSetOf<String>()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    abstract fun viewDidAppear()

    open fun viewDidDisappear() {
        cancelScope()
    }

    fun launchScope(
        coroutineContext: CoroutineContext = Dispatchers.Default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch{
            val uuid = Scope.launch(coroutineContext, start, block).first
            mutex.withLock {
                viewJobs.add(uuid)
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