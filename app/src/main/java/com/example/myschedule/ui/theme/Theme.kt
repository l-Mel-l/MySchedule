package com.example.myschedule.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Цвета для Темной темы
private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    secondary = OrangeLight,
    tertiary = OrangeDark,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = WhiteText,
    onBackground = WhiteText,
    onSurface = WhiteText
)

// Цвета для Светлой темы
private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = OrangeDark,
    tertiary = OrangeLight,
    background = WhiteBackground,
    surface = WhiteBackground,
    onPrimary = BlackText,
    onBackground = BlackText,
    onSurface = BlackText
)

@Composable
fun MyScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color - это фишка Android 12+, когда цвета берутся из обоев
    // Мы пока отключим её (false), чтобы у всех был твой оранжевый дизайн
    dynamicColor: Boolean = false,
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

    // Этот код красит строку состояния (где часы и батарейка) в цвет приложения
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Верхняя панель (часы) - под цвет фона
            window.statusBarColor = colorScheme.background.toArgb()

            // Нижняя системная панель - ТОЖЕ под цвет фона (background)
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}