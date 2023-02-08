package io.redlink.more.more_app_mutliplatform

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val productName: String = UIDevice.currentDevice.model
    //override val deviceId: String = UIDevice.currentDevice.identifierForVendor.toString()
}

actual fun getPlatform(): Platform = IOSPlatform()