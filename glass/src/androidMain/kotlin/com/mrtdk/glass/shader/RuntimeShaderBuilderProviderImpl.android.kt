package com.mrtdk.glass.shader

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import com.mrtdk.glass.shader.contract.RuntimeShaderBuilderProvider

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
actual fun getRuntimeShaderBuilderProvider(): RuntimeShaderBuilderProvider = RuntimeShaderBuilderProviderImpl()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class RuntimeShaderBuilderProviderImpl : RuntimeShaderBuilderProvider {
    private val runtimeShaderBuilder = getRuntimeShaderBuilder() as RuntimeShader

    override fun build(): RuntimeShader = runtimeShaderBuilder
}