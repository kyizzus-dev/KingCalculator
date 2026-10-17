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

private val DarkColorScheme = darkColorScheme(
  primary = CalcPrimaryDark,
  onPrimary = CalcOnPrimaryDark,
  primaryContainer = CalcPrimaryContainerDark,
  onPrimaryContainer = CalcOnPrimaryContainerDark,
  secondary = CalcSecondaryDark,
  onSecondary = CalcOnSecondaryDark,
  secondaryContainer = CalcSecondaryContainerDark,
  onSecondaryContainer = CalcOnSecondaryContainerDark,
  tertiary = CalcTertiaryDark,
  onTertiary = CalcOnTertiaryDark,
  background = CalcBackgroundDark,
  surface = CalcSurfaceDark,
  surfaceVariant = CalcSurfaceVariantDark,
  onSurface = CalcOnSurfaceDark,
  outline = CalcOutlineDark
)

private val LightColorScheme = lightColorScheme(
  primary = CalcPrimaryLight,
  onPrimary = CalcOnPrimaryLight,
  primaryContainer = CalcPrimaryContainerLight,
  onPrimaryContainer = CalcOnPrimaryContainerLight,
  secondary = CalcSecondaryLight,
  onSecondary = CalcOnSecondaryLight,
  secondaryContainer = CalcSecondaryContainerLight,
  onSecondaryContainer = CalcOnSecondaryContainerLight,
  tertiary = CalcTertiaryLight,
  onTertiary = CalcOnTertiaryLight,
  background = CalcBackgroundLight,
  surface = CalcSurfaceLight,
  surfaceVariant = CalcSurfaceVariantLight,
  onSurface = CalcOnSurfaceLight,
  outline = CalcOutlineLight
)

@Composable
fun CalcAlgebraTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent tailored math aesthetics
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
