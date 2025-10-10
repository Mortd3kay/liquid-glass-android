package com.mrtdk.glass.shader.contract

import androidx.compose.ui.geometry.Size

interface ShaderUniformProvider {
    fun uniform(name: String, value: Int)
    fun uniform(name: String, value: Float)
    fun uniform(name: String, value1: Float, value2: Float)
    fun uniform(name: String, value: FloatArray)
    fun resolution(): Size
}