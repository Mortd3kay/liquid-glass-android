package com.mrtdk.glass.shader

import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider
import com.mrtdk.glass.shader.contract.ShaderUniformProvider

expect fun getShaderUniformProvider(runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider): ShaderUniformProvider

fun uniformShaderName(): String = "content"