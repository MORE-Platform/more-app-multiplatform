package io.redlink.more.more_app_mutliplatform.viewModels

import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class CoreViewModel: Closeable {
    protected val viewModelScope = CoroutineScope(Job() + Dispatchers.Default)
    private var viewJobs = mutableSetOf<Job>()

    abstract fun viewDidAppear()

    open fun viewDidDisappear() {
        cancelScope()
    }

    fun launchScope(coroutineContext: CoroutineContext = Dispatchers.Default, start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit) {
        viewJobs.add(viewModelScope.launch(coroutineContext, start, block))
    }

    fun cancelScope() {
        viewJobs.forEach { it.cancel() }
        viewJobs.clear()
    }

    override fun close() {
        viewModelScope.cancel()
    }
}