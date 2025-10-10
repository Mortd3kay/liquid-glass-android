package com.mrtdk.glass

import android.os.Build

class AndroidPlatform : Platform {
    override val version: Int = Build.VERSION.SDK_INT
    override val type: PlatformType = PlatformType.ANDROID
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val TIRAMISU: Int
    get() = Build.VERSION_CODES.TIRAMISU