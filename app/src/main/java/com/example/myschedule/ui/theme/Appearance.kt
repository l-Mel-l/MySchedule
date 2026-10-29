package com.example.myschedule.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object Appearance {
    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    const val STYLE_CLASSIC = 0
    const val STYLE_OCEAN = 1
    const val STYLE_FOREST = 2
    const val STYLE_PLUM = 3
    const val STYLE_SILVER = 4
    const val STYLE_CUSTOM = 5

    class StyleDef(val id: Int, val name: String, val seed: Int)

    val styles = listOf(
        StyleDef(STYLE_CLASSIC, "Классика", 0xFFFFA500.toInt()),
        StyleDef(STYLE_OCEAN, "Океан", 0xFF1E88E5.toInt()),
        StyleDef(STYLE_FOREST, "Лес", 0xFF43A047.toInt()),
        StyleDef(STYLE_PLUM, "Слива", 0xFF8E24AA.toInt()),
        StyleDef(STYLE_SILVER, "Серебро", 0xFF90A4AE.toInt())
    )

    var themeMode by mutableIntStateOf(THEME_SYSTEM)
        private set
    var styleId by mutableIntStateOf(STYLE_CLASSIC)
        private set
    var customSeed by mutableIntStateOf(0xFF1E88E5.toInt())
        private set
    var pureBlack by mutableStateOf(false)
        private set
    var tintBackground by mutableStateOf(true)
        private set

    var accent by mutableIntStateOf(0)
        private set
    var lessonCard by mutableIntStateOf(0)
        private set
    var timerRing by mutableIntStateOf(0)
        private set

    private var loaded = false

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences("appearance_prefs", Context.MODE_PRIVATE)

    fun seedColor(): Int =
        if (styleId == STYLE_CUSTOM) customSeed
        else styles.firstOrNull { it.id == styleId }?.seed ?: styles.first().seed

    fun load(context: Context) {
        if (loaded) return
        val p = prefs(context)
        themeMode = p.getInt("theme_mode", THEME_SYSTEM)
        styleId = p.getInt("style_id", STYLE_CLASSIC)
        customSeed = p.getInt("custom_seed", 0xFF1E88E5.toInt())
        pureBlack = p.getBoolean("pure_black", false)
        tintBackground = p.getBoolean("tint_background", true)
        accent = p.getInt("accent", 0)
        lessonCard = p.getInt("lesson_card", 0)
        timerRing = p.getInt("timer_ring", 0)
        loaded = true
    }

    fun setThemeMode(context: Context, value: Int) {
        themeMode = value
        prefs(context).edit().putInt("theme_mode", value).apply()
    }

    fun setStyle(context: Context, id: Int) {
        styleId = id
        accent = 0
        lessonCard = 0
        timerRing = 0
        prefs(context).edit()
            .putInt("style_id", id)
            .putInt("accent", 0)
            .putInt("lesson_card", 0)
            .putInt("timer_ring", 0)
            .apply()
    }

    fun setCustomSeed(context: Context, argb: Int, save: Boolean = true) {
        customSeed = argb
        if (save) prefs(context).edit().putInt("custom_seed", argb).apply()
    }

    fun saveCustomSeed(context: Context) {
        prefs(context).edit().putInt("custom_seed", customSeed).apply()
    }

    fun setPureBlack(context: Context, value: Boolean) {
        pureBlack = value
        prefs(context).edit().putBoolean("pure_black", value).apply()
    }

    fun setTintBackground(context: Context, value: Boolean) {
        tintBackground = value
        prefs(context).edit().putBoolean("tint_background", value).apply()
    }

    fun setAccent(context: Context, argb: Int) {
        accent = argb
        prefs(context).edit().putInt("accent", argb).apply()
    }

    fun setLessonCard(context: Context, argb: Int) {
        lessonCard = argb
        prefs(context).edit().putInt("lesson_card", argb).apply()
    }

    fun setTimerRing(context: Context, argb: Int) {
        timerRing = argb
        prefs(context).edit().putInt("timer_ring", argb).apply()
    }

    fun resetAll(context: Context) {
        setThemeMode(context, THEME_SYSTEM)
        setStyle(context, STYLE_CLASSIC)
        setPureBlack(context, false)
        setTintBackground(context, true)
    }

    fun onColorFor(background: Color): Color =
        if (background.luminance() > 0.55f) Color(0xFF1C1B1F) else Color.White
}
