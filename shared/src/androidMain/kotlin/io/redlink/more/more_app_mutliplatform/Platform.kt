package io.redlink.more.more_app_mutliplatform

import android.provider.Settings

class AndroidPlatform : Platform {

    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    override val productName: String
        get() = android.os.Build.PRODUCT
}

actual fun getPlatform(): Platform = AndroidPlatform()