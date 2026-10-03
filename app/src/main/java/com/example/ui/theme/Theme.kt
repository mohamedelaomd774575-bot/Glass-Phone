package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = IosBlue,
  onPrimary = Color.White,
  primaryContainer = IosBlue.copy(alpha = 0.25f),
  onPrimaryContainer = Color.White,
  secondary = IosGreen,
  onSecondary = Color.White,
  error = IosRed,
  onError = Color.White,
  background = Color.Transparent,
  onBackground = Color.White,
  surface = Color.Transparent,
  onSurface = Color.White,
  surfaceVariant = Color.White.copy(alpha = 0.12f),
  onSurfaceVariant = Color.White.copy(alpha = 0.7f),
  outline = Color.White.copy(alpha = 0.35f)
)

private val LightColorScheme = lightColorScheme(
  primary = IosBlue,
  onPrimary = Color.White,
  primaryContainer = IosBlue.copy(alpha = 0.25f),
  onPrimaryContainer = Color(0xFF003880),
  secondary = IosGreen,
  onSecondary = Color.White,
  error = IosRed,
  onError = Color.White,
  background = Color.Transparent,
  onBackground = Color(0xFF1C1C1E),
  surface = Color.Transparent,
  onSurface = Color(0xFF1C1C1E),
  surfaceVariant = Color.White.copy(alpha = 0.5f),
  onSurfaceVariant = Color(0xFF3C3C43),
  outline = Color.White.copy(alpha = 0.6f)
)

@Composable
fun PhoneTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = !darkTheme
          isAppearanceLightNavigationBars = !darkTheme
        }
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
