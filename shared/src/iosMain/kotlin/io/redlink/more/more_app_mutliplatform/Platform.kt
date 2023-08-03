package io.redlink.more.more_app_mutliplatform

import platform.UIKit.UIDevice

actual fun getPlatform(): Platform = Platform(
    name = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion,
    productName = UIDevice.currentDevice.model
)