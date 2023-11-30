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
package io.redlink.more.app.android.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <T> MutableStateFlow<Set<T>>.append(value: T) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.add(value)) {
        val context = this
        CoroutineScope(Job() + Dispatchers.Default).launch {
            context.emit(mutableCollection)
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

fun <T> MutableStateFlow<Set<T>>.remove(value: T) {
    val mutableCollection = this.value.toMutableSet()
    if (mutableCollection.remove(value)) {
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