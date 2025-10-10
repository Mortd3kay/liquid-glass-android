package com.mrtdk.glass.shader

import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider
import org.jetbrains.skia.RuntimeShaderBuilder

actual fun getRuntimeShaderBuilderProvider(): RuntimeShaderBuilderProvider = RuntimeShaderBuilderProviderImpl()

internal class RuntimeShaderBuilderProviderImpl : RuntimeShaderBuilderProvider {
    private val runtimeShaderBuilder = getRuntimeShaderBuilder() as RuntimeShaderBuilder

    override fun build(): RuntimeShaderBuilder = runtimeShaderBuilder
}