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
package io.redlink.more.viewModels

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.Closeable
import io.redlink.more.logging.event
import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.scopes.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class CoreViewModel : Closeable {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    abstract fun viewIdentifier(): String

    open fun viewOpened() {
        Napier.event(LogEvent.VIEW_OPEN, viewIdentifier())
    }

    open fun viewClosed() {
        Napier.event(LogEvent.VIEW_CLOSED, viewIdentifier())
    }

    open fun viewDidAppear() {
        viewOpened()
    }

    open fun viewDidDisappear() {
        viewClosed()
    }

    fun launchScope(
        coroutineContext: CoroutineContext? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(
            coroutineContext ?: AppDispatchers.default,
            block = block
        )
    }

    override fun close() {
        viewModelScope.cancel()
    }
}