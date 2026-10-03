package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BlobBlue
import com.example.ui.theme.BlobMint
import com.example.ui.theme.BlobPink
import com.example.ui.theme.DarkBaseGradientBottom
import com.example.ui.theme.DarkBaseGradientTop
import com.example.ui.theme.LightBaseGradientBottom
import com.example.ui.theme.LightBaseGradientTop
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

/**
 * Vivid, static (animation-free) background featuring 3 large blurred blobs:
 * Blue (#5B8CFF), Pink (#FF6FB0), Mint (#34E0B0), designed so that translucent
 * Liquid Glass surfaces have rich, colorful backdrops to blur.
 */
@Composable
fun LiquidBackground(
  hazeState: HazeState,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val isDark = isSystemInDarkTheme()

  val baseTop = if (isDark) DarkBaseGradientTop else LightBaseGradientTop
  val baseBottom = if (isDark) DarkBaseGradientBottom else LightBaseGradientBottom

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(baseTop, baseBottom)
        )
      )
      .drawBehind {
        val w = size.width
        val h = size.height

        // Blob 1: Blue #5b8cff (upper left / center)
        val blueCenter = Offset(w * 0.28f, h * 0.22f)
        val blueRadius = w * 0.72f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              BlobBlue.copy(alpha = if (isDark) 0.68f else 0.55f),
              BlobBlue.copy(alpha = if (isDark) 0.35f else 0.25f),
              Color.Transparent
            ),
            center = blueCenter,
            radius = blueRadius
          ),
          center = blueCenter,
          radius = blueRadius
        )

        // Blob 2: Pink #ff6fb0 (mid right / center right)
        val pinkCenter = Offset(w * 0.82f, h * 0.44f)
        val pinkRadius = w * 0.76f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              BlobPink.copy(alpha = if (isDark) 0.65f else 0.50f),
              BlobPink.copy(alpha = if (isDark) 0.30f else 0.20f),
              Color.Transparent
            ),
            center = pinkCenter,
            radius = pinkRadius
          ),
          center = pinkCenter,
          radius = pinkRadius
        )

        // Blob 3: Mint #34e0b0 (bottom left / lower center)
        val mintCenter = Offset(w * 0.32f, h * 0.78f)
        val mintRadius = w * 0.82f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              BlobMint.copy(alpha = if (isDark) 0.62f else 0.48f),
              BlobMint.copy(alpha = if (isDark) 0.28f else 0.18f),
              Color.Transparent
            ),
            center = mintCenter,
            radius = mintRadius
          ),
          center = mintCenter,
          radius = mintRadius
        )

        // Subtle violet connector between blue and pink for organic depth
        val violetCenter = Offset(w * 0.65f, h * 0.28f)
        val violetRadius = w * 0.50f
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF8B5CF6).copy(alpha = if (isDark) 0.35f else 0.25f),
              Color.Transparent
            ),
            center = violetCenter,
            radius = violetRadius
          ),
          center = violetCenter,
          radius = violetRadius
        )
      }
      .haze(state = hazeState)
  ) {
    content()
  }
}
