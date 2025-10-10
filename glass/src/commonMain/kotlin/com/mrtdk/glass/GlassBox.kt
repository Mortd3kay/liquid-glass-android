package com.mrtdk.glass

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mrtdk.glass.effect.getRenderEffect
import com.mrtdk.glass.shader.getRuntimeShaderBuilderProvider
import com.mrtdk.glass.shader.getShaderUniformProvider
import kotlin.random.Random

internal data class GlassElement(
    val id: String,
    val position: Offset,
    val size: Size,
    val scale: Float,
    val blur: Float,
    val centerDistortion: Float,
    val cornerRadius: Float,
    val elevation: Float,
    val tint: Color,
    val darkness: Float,
    val warpEdges: Float,
) {
    // Check equality with tolerance for Float values
    fun equalsWithTolerance(other: GlassElement): Boolean {
        if (id != other.id) return false

        val tolerance = 0.01f
        val positionDiff = (position - other.position)
        val positionDistance =
            kotlin.math.sqrt(positionDiff.x * positionDiff.x + positionDiff.y * positionDiff.y)
        return positionDistance < tolerance &&
                kotlin.math.abs(size.width - other.size.width) < tolerance &&
                kotlin.math.abs(size.height - other.size.height) < tolerance &&
                kotlin.math.abs(scale - other.scale) < tolerance &&
                kotlin.math.abs(blur - other.blur) < tolerance &&
                kotlin.math.abs(centerDistortion - other.centerDistortion) < tolerance &&
                kotlin.math.abs(cornerRadius - other.cornerRadius) < tolerance &&
                kotlin.math.abs(elevation - other.elevation) < tolerance &&
                kotlin.math.abs(darkness - other.darkness) < tolerance &&
                kotlin.math.abs(warpEdges - other.warpEdges) < tolerance &&
                tint == other.tint
    }
}

interface GlassScope {
    fun Modifier.glassBackground(
        id: Long,
        scale: Float,
        blur: Float,
        centerDistortion: Float,
        shape: CornerBasedShape,
        elevation: Dp = 0.dp,
        tint: Color = Color.Transparent,
        darkness: Float = 0f,
        warpEdges: Float = 0f,
    ): Modifier
}

interface GlassBoxScope : BoxScope, GlassScope

@Composable
fun GlassBoxScope.GlassBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    @FloatRange(from = 0.0, to = 1.0)
    scale: Float = 0f,
    @FloatRange(from = 0.0, to = 1.0)
    blur: Float = 0f,
    @FloatRange(from = 0.0, to = 1.0)
    centerDistortion: Float = 0f,
    shape: CornerBasedShape = RoundedCornerShape(0.dp),
    elevation: Dp = 0.dp,
    tint: Color = Color.Transparent,
    @FloatRange(from = 0.0, to = 1.0)
    darkness: Float = 0f,
    @FloatRange(from = 0.0, to = 1.0)
    warpEdges: Float = 0f,
    content: @Composable BoxScope.() -> Unit = { },
) {
    val id = remember { Random.nextLong() }
    Box(
        modifier = modifier.glassBackground(
            id,
            scale.coerceIn(0f, 1f),
            blur.coerceIn(0f, 1f),
            centerDistortion.coerceIn(0f, 1f),
            shape,
            elevation,
            tint,
            darkness.coerceIn(0f, 1f),
            warpEdges.coerceIn(0f, 1f)
        ),
        contentAlignment, propagateMinConstraints, content
    )
}

private class GlassBoxScopeImpl(
    boxScope: BoxScope,
    glassScope: GlassScope
) : GlassBoxScope, BoxScope by boxScope, GlassScope by glassScope


private class GlassScopeImpl(private val density: Density) : GlassScope {

    var updateCounter by mutableIntStateOf(0)
    val elements: MutableList<GlassElement> = mutableListOf()
    private val activeElements = mutableSetOf<String>()

    fun markElementAsActive(elementId: String) {
        activeElements.add(elementId)
    }

    fun cleanupInactiveElements() {
        val elementsToRemove = elements.filter { it.id !in activeElements }
        if (elementsToRemove.isNotEmpty()) {
            elements.removeAll { it.id !in activeElements }
            updateCounter++
        }
        activeElements.clear()
    }

    override fun Modifier.glassBackground(
        id: Long,
        scale: Float,
        blur: Float,
        centerDistortion: Float,
        shape: CornerBasedShape,
        elevation: Dp,
        tint: Color,
        darkness: Float,
        warpEdges: Float,
    ): Modifier = this
        .background(color = Color.Transparent, shape = shape)
        .onGloballyPositioned { coordinates ->
            val elementId = "glass_$id"
            markElementAsActive(elementId)

            val position = coordinates.positionInRoot()
            val size = coordinates.size.toSize()

            val element = GlassElement(
                id = elementId,
                position = position,
                size = size,
                cornerRadius = shape.topStart.toPx(size, density),
                scale = scale,
                blur = blur,
                centerDistortion = centerDistortion,
                elevation = with(density) { elevation.toPx() },
                tint = tint,
                darkness = darkness,
                warpEdges = warpEdges,
            )

            // Find existing element with the same ID
            val existingIndex = elements.indexOfFirst { it.id == element.id }

            // Update only if an element changed
            if (existingIndex == -1) {
                elements.add(element)
                updateCounter++
            } else {
                // Check if the element changed with Float tolerance
                val existing = elements[existingIndex]
                if (!existing.equalsWithTolerance(element)) {
                    elements[existingIndex] = element
                    updateCounter++
                }
            }
        }
}

private class GlassScopeFallbackImpl(private val density: Density) : GlassScope {

    override fun Modifier.glassBackground(
        id: Long,
        scale: Float,
        blur: Float,
        centerDistortion: Float,
        shape: CornerBasedShape,
        elevation: Dp,
        tint: Color,
        darkness: Float,
        warpEdges: Float,
    ): Modifier {
        // Create a glass-like effect using available modifiers
        val glassTint = if (tint == Color.Transparent) {
            Color.White.copy(alpha = 0.1f)
        } else {
            tint.copy(alpha = (tint.alpha * 0.9f).coerceIn(0f, 1f))
        }

        // Create a darker overlay for the darkness effect
        val darknessOverlay = if (darkness > 0f) {
            Color.Black.copy(alpha = darkness * 0.3f)
        } else {
            Color.Transparent
        }

        // Create a gradient for a glass-like appearance
        val glassGradient = Brush.verticalGradient(
            colors = listOf(
                glassTint,
                glassTint.copy(alpha = glassTint.alpha * 0.7f),
                glassTint.copy(alpha = glassTint.alpha * 0.5f),
                glassTint
            )
        )

        return this
            // Apply glass gradient background
            .background(
                brush = glassGradient,
                shape = shape
            )
            // Apply darkness overlay if needed
            .let { modifier ->
                if (darknessOverlay != Color.Transparent) {
                    modifier.background(
                        color = darknessOverlay,
                        shape = shape
                    )
                } else {
                    modifier
                }
            }
            // Apply scale effect (limited simulation)
            .let { modifier ->
                if (scale > 0f) {
                    modifier.graphicsLayer {
                        scaleX = 1f + (scale * 0.1f)
                        scaleY = 1f + (scale * 0.1f)
                    }
                } else {
                    modifier
                }
            }
            // Apply transparency for warp edges effect
            .let { modifier ->
                if (warpEdges > 0f) {
                    modifier.alpha(1f - (warpEdges * 0.2f).coerceIn(0f, 0.8f))
                } else {
                    modifier
                }
            }
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    glassContent: @Composable GlassBoxScope.() -> Unit,
) {
    val platform = getPlatform()

    if ((platform.type == PlatformType.ANDROID && isModernAndroid(platform.version as Int)) ||
        platform.type == PlatformType.DESKTOP
    ) {
        GlassContainerWithShader(modifier, content, glassContent)
    } else {
        GlassContainerFallback(modifier, content, glassContent)
    }
}


@Composable
private fun GlassContainerWithShader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    glassContent: @Composable GlassBoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val glassScope = remember { GlassScopeImpl(density) }

    val runtimeShaderBuilderProvider = remember { mutableStateOf(getRuntimeShaderBuilderProvider()) }
    val shaderUniformProvider =
        remember { mutableStateOf(getShaderUniformProvider(runtimeShaderBuilderProvider.value)) }

    LaunchedEffect(glassScope.updateCounter) {
        runtimeShaderBuilderProvider.value = getRuntimeShaderBuilderProvider().apply {
            shaderUniformProvider.value = getShaderUniformProvider(this)
        }
    }


    SideEffect {
        glassScope.cleanupInactiveElements()
    }

    DisposableEffect(Unit) {
        onDispose {
            glassScope.elements.clear()
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                shaderUniformProvider.value.apply {
                    uniform("resolution", size.width, size.height)

                    val elements = glassScope.elements

                    val maxElements = 10
                    val positions = FloatArray(maxElements * 2)
                    val sizes = FloatArray(maxElements * 2)
                    val scales = FloatArray(maxElements)
                    val radii = FloatArray(maxElements)
                    val elevations = FloatArray(maxElements)
                    val centerDistortions = FloatArray(maxElements)
                    val tints = FloatArray(maxElements * 4)
                    val darkness = FloatArray(maxElements)
                    val warpEdges = FloatArray(maxElements)
                    val blurs = FloatArray(maxElements)


                    val elementsCount = minOf(elements.size, maxElements)
                    uniform("elementsCount", elementsCount)

                    for (i in 0 until elementsCount) {
                        val element = elements[i]
                        positions[i * 2] = element.position.x
                        positions[i * 2 + 1] = element.position.y
                        sizes[i * 2] = element.size.width
                        sizes[i * 2 + 1] = element.size.height
                        scales[i] = element.scale
                        radii[i] = element.cornerRadius
                        elevations[i] = element.elevation
                        centerDistortions[i] = element.centerDistortion

                        tints[i * 4] = element.tint.red
                        tints[i * 4 + 1] = element.tint.green
                        tints[i * 4 + 2] = element.tint.blue
                        tints[i * 4 + 3] = element.tint.alpha

                        darkness[i] = element.darkness
                        warpEdges[i] = element.warpEdges
                        blurs[i] = element.blur
                    }

                    uniform("glassPositions", positions)
                    uniform("glassSizes", sizes)
                    uniform("glassScales", scales)
                    uniform("cornerRadii", radii)
                    uniform("elevations", elevations)
                    uniform("centerDistortions", centerDistortions)
                    uniform("glassTints", tints)
                    uniform("glassDarkness", darkness)
                    uniform("glassWarpEdges", warpEdges)
                    uniform("glassBlurs", blurs)

                    renderEffect = getRenderEffect(runtimeShaderBuilderProvider.value)
                }
            }
    ) {
        content()
    }
    Box(modifier = modifier) {
        GlassBoxScopeImpl(this, glassScope).glassContent()
    }
}

@Composable
private fun GlassContainerFallback(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    glassContent: @Composable GlassBoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val glassScope = remember { GlassScopeFallbackImpl(density) }

    Box(modifier = modifier) {
        content()
    }
    Box(modifier = modifier) {
        GlassBoxScopeImpl(this, glassScope).glassContent()
    }
}