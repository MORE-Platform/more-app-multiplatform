package io.redlink.more.more_app_mutliplatform.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun CoroutineScope.repeatEveryFewSeconds(intervalMillis: Long, action: suspend () -> Unit): Job {
    return launch {
        while (isActive) {
            action()
            delay(intervalMillis)
        }
    }
}