package io.redlink.more.more_app_mutliplatform

import kotlinx.serialization.Serializable

@Serializable
data class Platform (
    val name: String,
    val productName: String
)

expect fun getPlatform(): Platform