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
package io.redlink.more.scopes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

interface MoreDispatchers {
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
}

object AppDispatchers : MoreDispatchers {
    private var _default: CoroutineDispatcher = Dispatchers.Default
    private var _main: CoroutineDispatcher = Dispatchers.Main
    private var _io: CoroutineDispatcher = Dispatchers.IO

    override val default: CoroutineDispatcher get() = _default
    override val main: CoroutineDispatcher get() = _main
    override val io: CoroutineDispatcher get() = _io

    fun set(
        default: CoroutineDispatcher = Dispatchers.Default,
        main: CoroutineDispatcher = Dispatchers.Main,
        io: CoroutineDispatcher = Dispatchers.IO
    ) {
        _default = default
        _main = main
        _io = io
    }

    fun reset() {
        _default = Dispatchers.Default
        _main = Dispatchers.Main
        _io = Dispatchers.IO
    }
}
