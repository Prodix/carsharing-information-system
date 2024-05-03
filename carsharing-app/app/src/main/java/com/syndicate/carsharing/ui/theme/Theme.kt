package com.syndicate.carsharing.ui.theme

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
    primary = Color(0xFF6699CC),
    secondary = Color(0xFF99CC99),
    tertiary = Color(0xFFB5B5B5),
    onPrimary = Color(0xFFF0F5FA),
    error = Color(0xFFFAF0F0),
    onError = Color(0xFFBB3E3E),
    onTertiary = Color(0xFF9E9E9E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6699CC),
    secondary = Color(0xFF99CC99),
    tertiary = Color(0xFFB5B5B5),
    onPrimary = Color(0xFFF0F5FA),
    error = Color(0xFFFAF0F0),
    onError = Color(0xFFBB3E3E),
    onTertiary = Color(0xFF9E9E9E),
    background = Color.White
)

@Composable
fun CarsharingTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}