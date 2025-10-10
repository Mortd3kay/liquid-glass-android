package com.mrtdk.glass

class DesktopPlatform: Platform {
    override val version: String = System.getProperty("java.version")
    override val type: PlatformType = PlatformType.DESKTOP
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual val TIRAMISU: Int
    get() = 0