package io.redlink.more.more_app_mutliplatform.util

import platform.Foundation.NSUUID

actual fun createUUID(): String = NSUUID.UUID().UUIDString