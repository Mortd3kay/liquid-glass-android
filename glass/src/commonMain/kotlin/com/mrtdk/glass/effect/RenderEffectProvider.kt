package com.mrtdk.glass.effect

import androidx.compose.ui.graphics.RenderEffect
import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider

expect fun getRenderEffect(runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider): RenderEffect