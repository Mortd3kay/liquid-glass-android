package com.mrtdk.glass.effect

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asComposeRenderEffect
import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider
import com.mrtdk.glass.shader.uniformShaderName

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
actual fun getRenderEffect(
    runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider
): androidx.compose.ui.graphics.RenderEffect {
    return RenderEffect.createRuntimeShaderEffect(
        runtimeShaderBuilderProvider.build() as RuntimeShader,
        uniformShaderName()
    ).asComposeRenderEffect()
}