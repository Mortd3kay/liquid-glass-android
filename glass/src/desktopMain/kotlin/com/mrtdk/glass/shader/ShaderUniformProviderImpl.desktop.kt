package com.mrtdk.glass.shader

import androidx.compose.ui.geometry.Size
import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider
import com.mrtdk.glass.shader.contract.ShaderUniformProvider
import org.jetbrains.skia.RuntimeShaderBuilder

actual fun getShaderUniformProvider(
    runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider
): ShaderUniformProvider = ShaderUniformProviderImpl(runtimeShaderBuilderProvider)

internal class ShaderUniformProviderImpl(
    runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider
) : ShaderUniformProvider {
    private val runtimeShaderBuilder = runtimeShaderBuilderProvider.build() as RuntimeShaderBuilder

    var resolution: Size = Size.Zero

    override fun uniform(name: String, value: Int) {
        runtimeShaderBuilder.uniform(name, value)
    }

    override fun uniform(name: String, value: Float) {
        runtimeShaderBuilder.uniform(name, value)
    }

    override fun uniform(name: String, value1: Float, value2: Float) {
        runtimeShaderBuilder.uniform(name, value1, value2)
    }

    override fun uniform(name: String, value: FloatArray) {
        runtimeShaderBuilder.uniform(name, value)
    }

    override fun resolution(): Size = resolution
}