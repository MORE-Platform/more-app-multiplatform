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
package io.redlink.more.more_app_mutliplatform.extensions

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.scopes.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

fun <T : Any?> Flow<T>.asClosure(provideNewState: ((T) -> Unit)): Closeable {
    val job = Scope.create()
    this.onEach {
        provideNewState(it)
    }.launchIn(CoroutineScope(Dispatchers.Main + job.second))
    return object : Closeable {
        override fun close() {
            job.second.cancel()
        }
    }
}

fun <T : Any?> MutableStateFlow<T>.asClosure(provideNewState: ((T) -> Unit)): Closeable {
    val job = Scope.create()
    this.onEach {
        it?.let {
            provideNewState(it)
        }
    }.launchIn(CoroutineScope(Dispatchers.Main + job.second))
    return object : Closeable {
        override fun close() {
            job.second.cancel()
        }
    }
}

fun <T : Any?> MutableStateFlow<T?>.asNullableClosure(provideNewState: ((T?) -> Unit)): Closeable {
    val job = Scope.create()
    this.onEach {
        provideNewState(it)
    }.launchIn(CoroutineScope(Dispatchers.Main + job.second))
    return object : Closeable {
        override fun close() {
            job.second.cancel()
        }
    }
}

fun <T : Any?> StateFlow<T?>.asNullableClosure(provideNewState: ((T?) -> Unit)): Closeable {
    val job = Scope.create()
    this.onEach {
        provideNewState(it)
    }.launchIn(CoroutineScope(Dispatchers.Main + job.second))
    return object : Closeable {
        override fun close() {
            job.second.cancel()
        }
    }
}

fun <T> MutableStateFlow<Set<T>>.append(value: T?) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.add(value ?: return)) {
        this.set(mutableCollection)
    }
}

fun <T> MutableStateFlow<Set<T>>.appendIfNotContains(value: T, includes: (T) -> Boolean) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.firstOrNull(includes) == null) {
        if (mutableCollection.add(value)) {
            this.set(mutableCollection)
        }
    }
}

fun <T> MutableStateFlow<Set<T>>.appendAll(value: Collection<T>) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.addAll(value)) {
        this.set(mutableCollection)
    }
}

fun <T> MutableStateFlow<Set<T>>.remove(value: T?) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.remove(value ?: return)) {
        this.set(mutableCollection)
    }
}

fun <T> MutableStateFlow<Set<T>>.removeWhere(includes: (T) -> Boolean) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.removeAll(includes)) {
        this.set(mutableCollection)
    }
}

fun <T> MutableStateFlow<Set<T>>.removeAll(values: Collection<T>) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.removeAll(values.toSet())) {
        this.set(mutableCollection)
    }
}

fun <T> MutableStateFlow<Set<T>>.clear() {
    if (this.value.isNotEmpty()) {
        this.set(emptySet())
    }
}

fun <T> MutableStateFlow<T>.set(value: T?) {
    value?.let {
        Scope.launch {
            emit(it)
        }
    }
}

fun <T> MutableStateFlow<T?>.setNullable(value: T?) {
    Scope.launch {
        emit(value)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T, K> StateFlow<T>.mapState(
    scope: CoroutineScope,
    transform: (data: T) -> K
): StateFlow<K> {
    return mapLatest {
        transform(it)
    }
        .stateIn(scope, SharingStarted.Eagerly, transform(value))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T, K> StateFlow<T>.mapState(
    scope: CoroutineScope,
    initialValue: K,
    transform: suspend (data: T) -> K
): StateFlow<K> {
    return mapLatest {
        transform(it)
    }
        .stateIn(scope, SharingStarted.Eagerly, initialValue)
}


