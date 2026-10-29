package com.example.myschedule.notification

import android.content.Context

class NotificationPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("timer_notification_prefs", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var allowSwipeAway: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_SWIPE, false)
        set(value) = prefs.edit().putBoolean(KEY_ALLOW_SWIPE, value).apply()

    var showNextLesson: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NEXT, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NEXT, value).apply()

    var showBreaks: Boolean
        get() = prefs.getBoolean(KEY_SHOW_BREAKS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_BREAKS, value).apply()

    var preLessonMinutes: Int
        get() = prefs.getInt(KEY_PRE_LESSON, 30)
        set(value) = prefs.edit().putInt(KEY_PRE_LESSON, value).apply()

    var cardStyle: Int
        get() = prefs.getInt(KEY_CARD_STYLE, STYLE_AUTO)
        set(value) = prefs.edit().putInt(KEY_CARD_STYLE, value).apply()

    companion object {
        const val STYLE_AUTO = 0
        const val STYLE_DARK = 1
        const val STYLE_LIGHT = 2
        private const val KEY_CARD_STYLE = "card_style"
        const val KEY_ENABLED = "enabled"
        const val KEY_ALLOW_SWIPE = "allow_swipe_away"
        const val KEY_SHOW_NEXT = "show_next_lesson"
        const val KEY_SHOW_BREAKS = "show_breaks"
        const val KEY_PRE_LESSON = "pre_lesson_minutes"
    }
}
