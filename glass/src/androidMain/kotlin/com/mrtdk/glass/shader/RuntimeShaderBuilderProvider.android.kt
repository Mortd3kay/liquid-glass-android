package com.mrtdk.glass.shader

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal actual fun getRuntimeShaderBuilder(): Any {
    return RuntimeShader(getShaderCode())
}