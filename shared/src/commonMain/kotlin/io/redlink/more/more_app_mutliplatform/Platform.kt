package io.redlink.more.more_app_mutliplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform