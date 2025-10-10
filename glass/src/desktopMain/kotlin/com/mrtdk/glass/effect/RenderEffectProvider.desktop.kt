package com.mrtdk.glass.effect

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider
import com.mrtdk.glass.shader.uniformShaderName
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeShaderBuilder

actual fun getRenderEffect(
    runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider
): RenderEffect {
    return ImageFilter.makeRuntimeShader(
        runtimeShaderBuilder = runtimeShaderBuilderProvider.build() as RuntimeShaderBuilder,
        shaderName = uniformShaderName(),
        input = null
    ).asComposeRenderEffect()
}