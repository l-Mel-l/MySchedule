package com.example.myschedule.glance

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.glance.color.ColorProvider
import com.example.myschedule.ui.theme.Appearance
import com.example.myschedule.ui.theme.styleColors

class WidgetPalette(
    val background: androidx.glance.unit.ColorProvider,
    val card: androidx.glance.unit.ColorProvider,
    val activeCard: androidx.glance.unit.ColorProvider,
    val text: androidx.glance.unit.ColorProvider,
    val textSecondary: androidx.glance.unit.ColorProvider,
    val cardText: androidx.glance.unit.ColorProvider,
    val cardTextSecondary: androidx.glance.unit.ColorProvider,
    val activeText: androidx.glance.unit.ColorProvider,
    val activeTextSecondary: androidx.glance.unit.ColorProvider,
    val accent: androidx.glance.unit.ColorProvider,
    val accentArgb: Int,
    val isDarkNow: Boolean
)

private class Raw(
    val bg: Color,
    val card: Color,
    val active: Color,
    val text: Color,
    val textSecondary: Color,
    val cardText: Color,
    val cardTextSecondary: Color,
    val activeText: Color,
    val activeTextSecondary: Color,
    val accent: Color
)

private fun raw(dark: Boolean): Raw {
    val s = styleColors(
        Appearance.styleId, Appearance.seedColor(), dark, Appearance.tintBackground, Appearance.pureBlack && dark
    )
    val accent = if (Appearance.accent != 0) Color(Appearance.accent) else s.accent
    val card = when {
        Appearance.lessonCard != 0 -> Color(Appearance.lessonCard)
        dark -> lerp(s.background, accent, 0.12f)
        else -> s.surface
    }
    val active = lerp(s.background, accent, if (dark) 0.32f else 0.22f)
    val text = Appearance.onColorFor(s.background)
    val cardText = Appearance.onColorFor(card)
    val activeText = Appearance.onColorFor(active)
    return Raw(
        bg = s.background,
        card = card,
        active = active,
        text = text,
        textSecondary = text.copy(alpha = 0.65f),
        cardText = cardText,
        cardTextSecondary = cardText.copy(alpha = 0.65f),
        activeText = activeText,
        activeTextSecondary = activeText.copy(alpha = 0.75f),
        accent = accent
    )
}

fun widgetPalette(context: Context): WidgetPalette {
    Appearance.load(context)

    val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    val darkNow = when (Appearance.themeMode) {
        Appearance.THEME_LIGHT -> false
        Appearance.THEME_DARK -> true
        else -> systemDark
    }

    val light = raw(false)
    val dark = raw(true)
    val forLight = if (Appearance.themeMode == Appearance.THEME_DARK) dark else light
    val forDark = if (Appearance.themeMode == Appearance.THEME_LIGHT) light else dark

    fun pick(f: (Raw) -> Color) = ColorProvider(day = f(forLight), night = f(forDark))

    val resolved = if (darkNow) forDark else forLight
    val accentForTimer = if (darkNow) resolved.accent else lerp(resolved.accent, Color.Black, 0.3f)

    return WidgetPalette(
        background = pick { it.bg },
        card = pick { it.card },
        activeCard = pick { it.active },
        text = pick { it.text },
        textSecondary = pick { it.textSecondary },
        cardText = pick { it.cardText },
        cardTextSecondary = pick { it.cardTextSecondary },
        activeText = pick { it.activeText },
        activeTextSecondary = pick { it.activeTextSecondary },
        accent = pick { it.accent },
        accentArgb = accentForTimer.toArgb(),
        isDarkNow = darkNow
    )
}

fun fixedColor(color: Color): androidx.glance.unit.ColorProvider = ColorProvider(day = color, night = color)
