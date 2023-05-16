package io.redlink.more.more_app_mutliplatform.viewModels

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class CoreViewModel : Closeable {
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
        viewJobs.add(Scope.launch(coroutineContext, start, block).first)
    }

    private fun cancelScope() {
        Scope.cancel(viewJobs)
        viewJobs.clear()
    }

    override fun close() {
        cancelScope()
    }
}