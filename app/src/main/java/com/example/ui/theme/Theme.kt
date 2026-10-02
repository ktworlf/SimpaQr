package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = CardBgLight,
    primaryContainer = AccentBgDark,
    onPrimaryContainer = AccentBgLight,
    secondary = AccentPrimary,
    onSecondary = CardBgLight,
    tertiary = SuccessGreen,
    background = CanvasBgDark,
    onBackground = TextPrimaryDark,
    surface = CardBgDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1F2430),
    onSurfaceVariant = TextSecondaryDark,
    outline = NavBorderDark,
    error = BadgeRed,
    onError = CardBgLight
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = CardBgLight,
    primaryContainer = AccentBgLight,
    onPrimaryContainer = AccentPrimary,
    secondary = AccentPrimary,
    onSecondary = CardBgLight,
    tertiary = SuccessGreen,
    background = CanvasBgLight,
    onBackground = TextPrimaryLight,
    surface = CardBgLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0x1F000000),
    error = BadgeRed,
    onError = CardBgLight
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun SimpaQrTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

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
