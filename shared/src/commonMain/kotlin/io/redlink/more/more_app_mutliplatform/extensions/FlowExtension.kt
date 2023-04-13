package io.redlink.more.more_app_mutliplatform.extensions

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T: Any?> Flow<T>.asClosure(provideNewState: ((T) -> Unit)): Closeable {
    val job = Job()
    this.onEach {
        provideNewState(it)
    }.launchIn(CoroutineScope(Dispatchers.Main + job))
    return object : Closeable {
        override fun close() {
            job.cancel()
        }
    }
}

fun <T: Any?> MutableStateFlow<T>.asClosure(provideNewState: ((T) -> Unit)): Closeable {
    val job = Job()
    this.onEach {
        it?.let {
            provideNewState(it)
        }
    }.launchIn(CoroutineScope(Dispatchers.Main + job))
    return object : Closeable {
        override fun close() {
            job.cancel()
        }
    }
}