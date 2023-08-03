package io.redlink.more.more_app_mutliplatform

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun napierDebugBuild(antilog: Antilog = DebugAntilog()) {
    Napier.base(antilog)
}