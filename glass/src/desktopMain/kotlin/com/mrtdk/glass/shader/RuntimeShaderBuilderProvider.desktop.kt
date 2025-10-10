package com.mrtdk.glass.shader

import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

internal actual fun getRuntimeShaderBuilder(): Any {
    return RuntimeShaderBuilder(
        effect = RuntimeEffect.makeForShader(getShaderCode())
    )
}
