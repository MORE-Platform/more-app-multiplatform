package io.redlink.more.more_app_mutliplatform

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun napierDebugBuild() {
    Napier.base(DebugAntilog())
}