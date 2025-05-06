package com.example.delhitransit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Composition local for ThemeManager access
val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("No ThemeManager provided")
}

// High Contrast Color Schemes
private val HighContrastDarkColorScheme = darkColorScheme(
    primary = HighContrastYellow,
    onPrimary = HighContrastBlack,
    primaryContainer = HighContrastYellow.copy(alpha = 0.8f),
    onPrimaryContainer = HighContrastBlack,
    secondary = HighContrastOrange,
    onSecondary = HighContrastBlack,
    secondaryContainer = HighContrastOrange.copy(alpha = 0.8f),
    onSecondaryContainer = HighContrastBlack,
    tertiary = HighContrastRed,
    onTertiary = HighContrastBlack,
    background = HighContrastBlack,
    onBackground = HighContrastWhite,
    surface = HighContrastBlack,
    onSurface = HighContrastWhite,
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = HighContrastWhite,
    error = HighContrastRed,
    onError = HighContrastBlack
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastBlue,
    onPrimary = HighContrastWhite,
    primaryContainer = HighContrastBlue.copy(alpha = 0.8f),
    onPrimaryContainer = HighContrastWhite,
    secondary = HighContrastOrange,
    onSecondary = HighContrastBlack,
    secondaryContainer = HighContrastOrange.copy(alpha = 0.8f),
    onSecondaryContainer = HighContrastBlack,
    tertiary = HighContrastRed,
    onTertiary = HighContrastWhite,
    background = HighContrastWhite,
    onBackground = HighContrastBlack,
    surface = HighContrastWhite,
    onSurface = HighContrastBlack,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = HighContrastBlack,
    error = HighContrastRed,
    onError = HighContrastWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = DelhiBlue.copy(alpha = 0.8f),
    onPrimary = Color.White,
    primaryContainer = DelhiBlue.copy(alpha = 0.6f),
    onPrimaryContainer = Color.White,
    secondary = DelhiOrange.copy(alpha = 0.8f),
    onSecondary = Color.Black,
    secondaryContainer = DelhiOrange.copy(alpha = 0.6f),
    onSecondaryContainer = Color.Black,
    tertiary = DelhiRed.copy(alpha = 0.8f),
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurfaceColor,
    onSurface = Color.White,
    surfaceVariant = DarkCardBackground,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DelhiBlue,
    onPrimary = Color.White,
    primaryContainer = DelhiBlue.copy(alpha = 0.7f),
    onPrimaryContainer = Color.White,
    secondary = DelhiOrange,
    onSecondary = Color.Black,
    secondaryContainer = DelhiOrange.copy(alpha = 0.7f),
    onSecondaryContainer = Color.Black,
    tertiary = DelhiRed,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimary,
    surface = SurfaceColor,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun DelhiTransitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to ensure our custom colors are used
    themeManager: ThemeManager? = null,
    content: @Composable () -> Unit
) {
    // Create or use provided ThemeManager
    val context = LocalContext.current
    val themeManagerInstance = remember {
        themeManager ?: ThemeManager(context)
    }
    val isDarkMode by themeManagerInstance.isDarkMode.collectAsState()
    val isHighContrastMode by themeManagerInstance.isHighContrastMode.collectAsState()

    // Use sensor-based dark mode or system default if auto mode is disabled
    val darkThemeState = isDarkMode || (darkTheme && !themeManagerInstance.isAutoMode.collectAsState().value)

    val colorScheme = when {
        isHighContrastMode -> if (darkThemeState) HighContrastDarkColorScheme else HighContrastLightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkThemeState) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkThemeState -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkThemeState
        }
    }

    // Cleanup ThemeManager when no longer needed
    DisposableEffect(Unit) {
        onDispose {
            themeManagerInstance.cleanup()
        }
    }

    CompositionLocalProvider(LocalThemeManager provides themeManagerInstance) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}