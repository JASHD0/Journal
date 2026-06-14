package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = LinenBrightPrimary,
    secondary = MintSoftSecondary,
    tertiary = SoftClayTertiary,
    background = SlateDarkBackground,
    surface = SlateDarkSurface,
    onPrimary = SlateDarkBackground,
    onSecondary = SlateDarkBackground,
    onTertiary = SlateDarkBackground,
    onBackground = LinenBrightPrimary,
    onSurface = LinenBrightPrimary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DeepCharcoalPrimary,
    secondary = SageSecondary,
    tertiary = TerracottaTertiary,
    background = WarmPaperBackground,
    surface = WarmPaperSurface,
    onPrimary = WarmPaperBackground,
    onSecondary = WarmPaperBackground,
    onTertiary = WarmPaperBackground,
    onBackground = DeepCharcoalPrimary,
    onSurface = DeepCharcoalPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set default dynamicColor to false to maintain our hand-crafted aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
