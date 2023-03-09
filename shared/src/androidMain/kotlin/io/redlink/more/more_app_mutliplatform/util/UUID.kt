package io.redlink.more.more_app_mutliplatform.util

import java.util.UUID

actual fun createUUID(): String = UUID.randomUUID().toString()