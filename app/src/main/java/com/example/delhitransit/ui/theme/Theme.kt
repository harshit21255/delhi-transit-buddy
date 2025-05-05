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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat



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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}