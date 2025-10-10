package com.mrtdk.glass.shader

import org.intellij.lang.annotations.Language

fun getShaderCode(): String = SHADER_CODE

@Language("AGSL")
private const val SHADER_CODE = """
uniform float2 resolution;
uniform shader content;
uniform int elementsCount;
uniform float2 glassPositions[10];
uniform float2 glassSizes[10];
uniform float glassScales[10];
uniform float cornerRadii[10];
uniform float elevations[10];
uniform float centerDistortions[10];
uniform float glassTints[40]; // 10 elements * 4 components (r,g,b,a)
uniform float glassDarkness[10];
uniform float glassWarpEdges[10];
uniform float glassBlurs[10];

// Calculate signed distance field for rounded rectangle
float sdfRoundedRect(float2 p, float2 floatSize, float radius) {
    float2 d = abs(p) - floatSize + radius;
    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - radius;
}

// Check if pixel is in warp region (0.0 = inner, 1.0 = warp zone)
float getWarpRegion(float2 localCoord, float2 floatSize, float cornerRadius, float warpEdges) {
    if (warpEdges <= 0.0) return 0.0;

    float outerSdf = sdfRoundedRect(localCoord, floatSize, cornerRadius);
    if (outerSdf >= 0.0) return 0.0;

    // Calculate inner boundary
    float inset = warpEdges * min(floatSize.x, floatSize.y) * 0.5;
    float2 innerSize = max(floatSize - inset, 0.1);
    float innerRadius = max(cornerRadius * min(innerSize.x / floatSize.x, innerSize.y / floatSize.y), 0.0);

    float innerSdf = sdfRoundedRect(localCoord, innerSize, innerRadius);
    return step(0.0, innerSdf);
}

// Apply barrel distortion in warp regions
float2 applyWarpDistortion(float2 localCoord, float2 floatSize, float cornerRadius, float warpEdges) {
    if (warpEdges <= 0.0) return localCoord;

    float inset = warpEdges * min(floatSize.x, floatSize.y) * 0.5;
    float2 innerSize = max(floatSize - inset, 0.1);
    float innerRadius = max(cornerRadius * min(innerSize.x / floatSize.x, innerSize.y / floatSize.y), 0.0);

    float innerSdf = sdfRoundedRect(localCoord, innerSize, innerRadius);
    if (innerSdf <= 0.0) return localCoord; // No distortion in inner region

    // Normalize distance for smooth distortion
    float normalizedDist = clamp(innerSdf / inset, 0.0, 1.0);
    float warpIntensity = normalizedDist * normalizedDist * warpEdges;

    // Apply barrel distortion
    float pullStrength = warpIntensity * 0.8;
    float targetScale = max(0.1, 1.0 - pullStrength);
    float2 pulledCoord = localCoord * targetScale;

    // Add radial distortion
    float2 centerDir = normalize(localCoord);
    float2 radialOffset = centerDir * (warpIntensity * 0.03 * length(localCoord));

    // Add swirl for strong warp
    if (warpEdges > 0.7 && normalizedDist > 0.8) {
        float angle = atan(localCoord.y, localCoord.x) + normalizedDist * warpEdges * 0.5;
        float r = length(pulledCoord);
        pulledCoord = float2(cos(angle), sin(angle)) * r;
    }

    return pulledCoord + radialOffset;
}

// Apply lens magnification effect
float2 applyLensEffect(float2 fragCoord, float2 center, float2 size, float cornerRadius, float scale, float centerDistortion) {
    if (scale <= 0.0) return fragCoord;

    float2 localCoord = fragCoord - center;
    float2 floatSize = size * 0.5;

    float sdf = sdfRoundedRect(localCoord, floatSize, cornerRadius);
    if (sdf >= 0.0) return fragCoord; // Outside lens

    // Calculate distortion based on distance from center
    float2 rel = localCoord / floatSize;
    float normalizedDist = length(rel) / 1.414; // Normalize to diagonal

    float baseScale = 1.0 + scale;
    float distortionFactor = 1.0;

    if (centerDistortion > 0.0) {
        float profile = 1.0 - smoothstep(0.0, 1.0, normalizedDist);
        distortionFactor = 1.0 + centerDistortion * profile;
    }

    float finalScale = baseScale * distortionFactor;
    return center + (fragCoord - center) / finalScale;
}

// Calculate shadow intensity
float getShadowIntensity(float2 localCoord, float2 floatSize, float cornerRadius, float elevation) {
    if (elevation <= 0.0) return 0.0;

    float shadowOffset = elevation * 0.5;
    float shadowBlur = elevation * 2.0;

    float2 shadowCoord = localCoord - float2(0.0, shadowOffset);
    float shadowSdf = sdfRoundedRect(shadowCoord, floatSize, cornerRadius);
    float originalSdf = sdfRoundedRect(localCoord, floatSize, cornerRadius);

    // Shadow only outside original element and within blur range
    if (originalSdf <= 0.0 || shadowSdf > shadowBlur) return 0.0;

    return (1.0 - shadowSdf / shadowBlur) * 0.15;
}

// Calculate rim highlight intensity
float getRimHighlight(float2 localCoord, float2 floatSize, float cornerRadius) {
    float sdf = sdfRoundedRect(localCoord, floatSize, cornerRadius);
    float rimWidth = 5.0;

    if (sdf <= 0.0 || sdf >= rimWidth) return 0.0;

    float intensity = (rimWidth - sdf) / rimWidth;
    float verticalPos = localCoord.y / floatSize.y;
    float lightingFactor = mix(1.2, 0.7, (verticalPos + 1.0) * 0.5);

    return intensity * 0.8 * lightingFactor;
}

float4 main(float2 fragCoord) {
    float2 finalCoord = fragCoord;
    float shadowAlpha = 0.0;
    float rimHighlight = 0.0;
    float4 tintColor = float4(0.0);
    float darknessEffect = 0.0;
    float blurRadius = 0.0;
    float2 surfaceNormal = float2(0.0);

    // Process each glass element
    for (int i = 0; i < 10; i++) {
        if (i >= elementsCount) break;
        float2 center = glassPositions[i] + glassSizes[i] * 0.5;
        float2 localCoord = fragCoord - center;
        float2 floatSize = glassSizes[i] * 0.5;
        float cornerRadius = cornerRadii[i];

        float sdf = sdfRoundedRect(localCoord, floatSize, cornerRadius);

        // Apply blur inside element
        if (sdf < 0.0 && glassBlurs[i] > 0.0) {
            blurRadius = max(blurRadius, glassBlurs[i] * 20.0);
        }

        // Apply warp and lens effects
        float warpRegion = getWarpRegion(localCoord, floatSize, cornerRadius, glassWarpEdges[i]);
        if (warpRegion > 0.0) {
            float2 warpedCoord = applyWarpDistortion(localCoord, floatSize, cornerRadius, glassWarpEdges[i]);
            float2 warpedFragCoord = center + warpedCoord;
            finalCoord = applyLensEffect(warpedFragCoord, glassPositions[i] + glassSizes[i] * 0.5,
                                       glassSizes[i], cornerRadius, glassScales[i], centerDistortions[i]);
        } else {
            finalCoord = applyLensEffect(finalCoord, center, glassSizes[i], cornerRadius,
                                       glassScales[i], centerDistortions[i]);
        }

        // Accumulate effects
        shadowAlpha = max(shadowAlpha, getShadowIntensity(localCoord, floatSize, cornerRadius, elevations[i]));
        rimHighlight = max(rimHighlight, getRimHighlight(localCoord, floatSize, cornerRadius));

        // Store surface normal for rim highlight
        if (sdf > 0.0 && sdf < 4.0 && surfaceNormal.x == 0.0 && surfaceNormal.y == 0.0) {
            float epsilon = 1.0;
            float sdfX = sdfRoundedRect(localCoord + float2(epsilon, 0.0), floatSize, cornerRadius);
            float sdfY = sdfRoundedRect(localCoord + float2(0.0, epsilon), floatSize, cornerRadius);
            surfaceNormal = normalize(float2(sdfX - sdf, sdfY - sdf));
        }

        // Apply tint and darkness inside element
        if (sdf < 0.0) {
            float4 elementTint = float4(glassTints[i * 4], glassTints[i * 4 + 1],
                                      glassTints[i * 4 + 2], glassTints[i * 4 + 3]);
            if (elementTint.a > 0.0) {
                tintColor = mix(tintColor, elementTint, elementTint.a);
            }

            // Apply darkness from edges inward
            float currentDarkness = glassDarkness[i];
            if (currentDarkness > 0.0) {
                float maxRadius = min(floatSize.x, floatSize.y) * 0.8;
                float distanceFromEdge = abs(sdf);
                if (distanceFromEdge < maxRadius) {
                    float intensity = smoothstep(0.0, 1.0, (maxRadius - distanceFromEdge) / maxRadius);
                    darknessEffect = max(darknessEffect, currentDarkness * intensity);
                }
            }
        }
    }

    // Sample background
    float4 color = content.eval(finalCoord);

    // Apply blur
    if (blurRadius > 0.0) {
        float4 blurredColor = float4(0.0);
        float totalWeight = 0.0;
        float invRadius = 1.0 / max(blurRadius, 1.0);

        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                float2 offset = float2(float(dx), float(dy)) * blurRadius * 0.4;
                float distance = length(offset) * invRadius;
                float weight = exp(-distance * distance * 2.0);
                blurredColor += content.eval(finalCoord + offset) * weight;
                totalWeight += weight;
            }
        }
        color = blurredColor / totalWeight;
    }

    if (tintColor.a > 0.0) {
        color.rgb = mix(color.rgb, tintColor.rgb, tintColor.a * 0.9);
    }

    if (darknessEffect > 0.0) {
        color.rgb = mix(color.rgb, float3(0.0), darknessEffect * 0.5);
    }

    // Apply rim highlight with reflection
    if (rimHighlight > 0.0) {
        float2 reflectionOffset = surfaceNormal * 24.0;
        float4 reflectedColor = content.eval(fragCoord + reflectionOffset);
        reflectedColor.rgb = max(reflectedColor.rgb * 1.8 + 0.35, 0.15);
        color = mix(color, reflectedColor, rimHighlight);
    }

    if (shadowAlpha > 0.0) {
        color.rgb = mix(color.rgb, float3(0.0), shadowAlpha);
    }

    return color;
}
"""
