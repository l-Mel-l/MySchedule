package com.example.myschedule.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
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

class StyleColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val accent: Color,
    val container: Color
)

fun styleColors(styleId: Int, seed: Int, dark: Boolean, tinted: Boolean, pureBlack: Boolean): StyleColors {
    val base = if (dark) DarkColorScheme else LightColorScheme
    val classic = styleId == Appearance.STYLE_CLASSIC

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed, hsl)
    val h = hsl[0]
    val s = hsl[1]
    val l = hsl[2]
    fun c(sat: Float, light: Float) =
        Color(ColorUtils.HSLToColor(floatArrayOf(h, sat.coerceIn(0f, 1f), light.coerceIn(0f, 1f))))

    val useTint = tinted && !classic
    val bgS = (s * 0.4f).coerceIn(0.04f, 0.30f)

    val accent = when {
        classic -> base.primary
        dark -> c(s.coerceAtMost(0.85f), maxOf(l, 0.6f))
        else -> c(s, minOf(l, 0.45f))
    }
    val container = when {
        classic -> base.primaryContainer
        dark -> c(s * 0.6f, 0.25f)
        else -> c(s * 0.7f, 0.85f)
    }

    val bg: Color
    val surface: Color
    val variant: Color
    if (dark) {
        bg = if (pureBlack) Color.Black else if (useTint) c(bgS, 0.07f) else base.background
        surface = when {
            useTint -> c(bgS, if (pureBlack) 0.10f else 0.13f)
            pureBlack -> Color(0xFF111111)
            else -> base.surface
        }
        variant = when {
            useTint -> c(bgS, if (pureBlack) 0.15f else 0.19f)
            pureBlack -> Color(0xFF1C1C1C)
            else -> base.surfaceVariant
        }
    } else {
        val lightS = (s * 0.7f).coerceIn(0.08f, 0.5f)
        bg = if (useTint) c(lightS, 0.92f) else base.background
        surface = if (useTint) c(lightS, 0.975f) else base.surface
        variant = if (useTint) c(lightS, 0.86f) else base.surfaceVariant
    }
    return StyleColors(bg, surface, variant, accent, container)
}

@Composable
fun MyScheduleTheme(
    darkTheme: Boolean = when (Appearance.themeMode) {
        Appearance.THEME_LIGHT -> false
        Appearance.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    },
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val style = styleColors(
        styleId = Appearance.styleId,
        seed = Appearance.seedColor(),
        dark = darkTheme,
        tinted = Appearance.tintBackground,
        pureBlack = Appearance.pureBlack && darkTheme
    )
    var colorScheme: ColorScheme = baseScheme.copy(
        background = style.background,
        surface = style.surface,
        surfaceVariant = style.surfaceVariant
    )
    if (Appearance.styleId != Appearance.STYLE_CLASSIC) {
        colorScheme = colorScheme.copy(
            primary = style.accent,
            onPrimary = if (style.accent.luminance() > 0.6f) BlackText else WhiteText,
            primaryContainer = style.container,
            onPrimaryContainer = baseScheme.onBackground,
            surfaceTint = style.accent
        )
    }

    if (Appearance.accent != 0) {
        val accent = Color(Appearance.accent)
        colorScheme = colorScheme.copy(
            primary = accent,
            onPrimary = if (accent.luminance() > 0.6f) BlackText else WhiteText,
            primaryContainer = androidx.compose.ui.graphics.lerp(colorScheme.background, accent, 0.28f),
            onPrimaryContainer = baseScheme.onBackground,
            surfaceTint = accent
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.background.toArgb()

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
