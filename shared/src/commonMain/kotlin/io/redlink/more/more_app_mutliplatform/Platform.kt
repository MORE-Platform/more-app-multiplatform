package io.redlink.more.more_app_mutliplatform

import kotlinx.serialization.Serializable

interface Platform {
    val name: String
    val productName: String
}

expect fun getPlatform(): Platform