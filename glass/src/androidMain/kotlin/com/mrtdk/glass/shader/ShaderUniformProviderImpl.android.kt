package com.mrtdk.glass.shader

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Size
import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider
import com.mrtdk.glass.shader.contract.ShaderUniformProvider

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
actual fun getShaderUniformProvider(
    runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider
): ShaderUniformProvider = ShaderUniformProviderImpl(runtimeShaderBuilderProvider)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class ShaderUniformProviderImpl(
    runtimeShaderBuilderProvider: RuntimeShaderBuilderProvider
) : ShaderUniformProvider {
    private val runtimeShaderBuilder = runtimeShaderBuilderProvider.build() as RuntimeShader

    var resolution: Size = Size.Zero

    override fun uniform(name: String, value: Int) {
        runtimeShaderBuilder.setIntUniform(name, value)
    }

    override fun uniform(name: String, value: Float) {
        runtimeShaderBuilder.setFloatUniform(name, value)
    }

    override fun uniform(name: String, value1: Float, value2: Float) {
        runtimeShaderBuilder.setFloatUniform(name, value1, value2)
    }

    override fun uniform(name: String, value: FloatArray) {
        runtimeShaderBuilder.setFloatUniform(name, value)
    }

    override fun resolution(): Size = resolution
}