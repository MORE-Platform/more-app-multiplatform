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
import kotlinx.coroutines.launch

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

fun <T> MutableStateFlow<Set<T>>.append(value: T?) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.add(value ?: return)) {
        val context = this
        CoroutineScope(Job() + Dispatchers.Default).launch {
            context.emit(mutableCollection)
        }
    }
}

fun <T> MutableStateFlow<Set<T>>.appendIfNotContains(value: T, includes: (T) -> Boolean) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.firstOrNull(includes) == null) {
        if (mutableCollection.add(value)) {
            val context = this
            CoroutineScope(Job() + Dispatchers.Default).launch {
                context.emit(mutableCollection)
            }
        }
    }
}

fun <T> MutableStateFlow<Set<T>>.append(value: Collection<T>) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.addAll(value)) {
        val context = this
        CoroutineScope(Job() + Dispatchers.Default).launch {
            context.emit(mutableCollection)
        }
    }
}

fun <T> MutableStateFlow<Set<T>>.remove(value: T?) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.remove(value ?: return)) {
        val context = this
        CoroutineScope(Job() + Dispatchers.Default).launch {
            context.emit(mutableCollection)
        }
    }
}

fun <T> MutableStateFlow<Set<T>>.removeWhere(includes: (T) -> Boolean) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.removeAll(includes)) {
        val context = this
        CoroutineScope(Job() + Dispatchers.Default).launch {
            context.emit(mutableCollection)
        }
    }
}

fun <T> MutableStateFlow<Set<T>>.clear() {
    if (this.value.isNotEmpty()) {
        val context = this
        CoroutineScope(Job() + Dispatchers.Default).launch {
            context.emit(emptySet())
        }
    }
}

fun <T> MutableStateFlow<T>.set(value: T?) {
    value?.let {
        CoroutineScope(Job() + Dispatchers.Default).launch {
            emit(it)
        }
    }
}