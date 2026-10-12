package com.demopay.app

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DemoGreen = Color(0xFF1B8A3C)

private val LightColors = lightColorScheme(
    primary = DemoGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF3E3),
    onPrimaryContainer = Color(0xFF0B3D1B),
    error = Color(0xFFE65100),
    background = Color(0xFFF3F8F4),
    onBackground = Color(0xFF1B1C1F),
    surface = Color.White,
    onSurface = Color(0xFF1B1C1F),
    surfaceVariant = Color(0xFFE8F0EA),
    onSurfaceVariant = Color(0xFF55615A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF66D18A),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF17432A),
    onPrimaryContainer = Color(0xFFBDF0CD),
    error = Color(0xFFFFB74D),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE6E7EB),
    surface = Color(0xFF1A1F1C),
    onSurface = Color(0xFFE6E7EB),
    surfaceVariant = Color(0xFF262B28),
    onSurfaceVariant = Color(0xFFA7B0AA)
)

private val AmoledColors = darkColorScheme(
    primary = Color(0xFF66D18A),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF0F2A1A),
    onPrimaryContainer = Color(0xFFBDF0CD),
    error = Color(0xFFFFB74D),
    background = Color.Black,
    onBackground = Color(0xFFEDEEF1),
    surface = Color(0xFF0B0B0C),
    onSurface = Color(0xFFEDEEF1),
    surfaceVariant = Color(0xFF141816),
    onSurfaceVariant = Color(0xFF9AA39D)
)

@Composable
fun DemoPayTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val scheme = when (mode) {
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.AMOLED -> AmoledColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = mode == ThemeMode.LIGHT
            controller.isAppearanceLightNavigationBars = mode == ThemeMode.LIGHT
        }
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
