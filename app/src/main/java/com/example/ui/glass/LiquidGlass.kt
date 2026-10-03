package com.example.ui.glass

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

/**
 * Creates the authentic iOS 26 Liquid Glass modifier:
 * - Real backdrop blur using Haze (blur radius ~22.dp)
 * - Translucent white tint (12% to 38% alpha)
 * - 1dp light border (white at 30% to 75% alpha)
 * - Top-edge specular highlight (thin vertical gradient from 55% alpha white to transparent)
 * - Soft, deep drop shadow
 */
fun Modifier.liquidGlass(
  hazeState: HazeState,
  shape: Shape,
  blurRadius: Dp = 22.dp,
  tintAlpha: Float = 0.22f, // 12-38%
  borderAlpha: Float = 0.50f, // 30-75%
  specularAlpha: Float = 0.55f,
  shadowElevation: Dp = 10.dp,
  isDark: Boolean = true
): Modifier {
  val baseTint = if (isDark) {
    Color.White.copy(alpha = tintAlpha.coerceIn(0.12f, 0.38f))
  } else {
    Color.White.copy(alpha = (tintAlpha + 0.15f).coerceIn(0.20f, 0.45f))
  }

  val borderTop = Color.White.copy(alpha = borderAlpha.coerceIn(0.30f, 0.75f))
  val borderBottom = Color.White.copy(alpha = (borderAlpha * 0.40f).coerceIn(0.12f, 0.40f))

  val ambientShadow = if (isDark) Color(0x66000000) else Color(0x1F000000)
  val spotShadow = if (isDark) Color(0x8C000000) else Color(0x33000000)

  return this
    .shadow(
      elevation = shadowElevation,
      shape = shape,
      clip = false,
      ambientColor = ambientShadow,
      spotColor = spotShadow
    )
    .hazeChild(
      state = hazeState,
      shape = shape,
      style = HazeStyle(
        backgroundColor = baseTint,
        tint = HazeTint(baseTint),
        blurRadius = blurRadius
      )
    )
    .clip(shape)
    .drawWithContent {
      drawContent()
      // Top-edge specular highlight (thin vertical gradient overlay from white 55% alpha to transparent)
      val highlightHeight = 3.dp.toPx()
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.White.copy(alpha = specularAlpha),
            Color.White.copy(alpha = specularAlpha * 0.45f),
            Color.Transparent
          ),
          startY = 0f,
          endY = highlightHeight
        )
      )
    }
    .border(
      width = 1.dp,
      brush = Brush.verticalGradient(
        colors = listOf(borderTop, borderBottom)
      ),
      shape = shape
    )
}

/**
 * Convenience wrapper for a Liquid Glass surface.
 */
@Composable
fun GlassSurface(
  hazeState: HazeState,
  shape: Shape,
  modifier: Modifier = Modifier,
  blurRadius: Dp = 22.dp,
  tintAlpha: Float = 0.22f,
  borderAlpha: Float = 0.50f,
  specularAlpha: Float = 0.55f,
  shadowElevation: Dp = 10.dp,
  content: @Composable BoxScope.() -> Unit
) {
  val isDark = isSystemInDarkTheme()
  Box(
    modifier = modifier.liquidGlass(
      hazeState = hazeState,
      shape = shape,
      blurRadius = blurRadius,
      tintAlpha = tintAlpha,
      borderAlpha = borderAlpha,
      specularAlpha = specularAlpha,
      shadowElevation = shadowElevation,
      isDark = isDark
    )
  ) {
    content()
  }
}
