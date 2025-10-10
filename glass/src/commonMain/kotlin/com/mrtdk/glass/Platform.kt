package com.mrtdk.glass

interface Platform {
    val version: Any
    val type: PlatformType
}

enum class PlatformType {
    ANDROID, DESKTOP
}

expect fun getPlatform(): Platform

expect val TIRAMISU: Int

fun isModernAndroid(version: Int): Boolean = version >= TIRAMISU