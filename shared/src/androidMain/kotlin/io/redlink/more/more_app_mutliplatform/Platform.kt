package io.redlink.more.more_app_mutliplatform

actual fun getPlatform(): Platform = Platform(
    name = "Android ${android.os.Build.VERSION.SDK_INT}",
    productName = android.os.Build.PRODUCT
)