package com.example.myschedule.notification

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.glance.appwidget.updateAll
import com.example.myschedule.glance.ScheduleListWidget
import com.example.myschedule.glance.ScheduleListWidgetReceiver
import com.example.myschedule.glance.ScheduleWidget
import com.example.myschedule.glance.ScheduleWidgetReceiver
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.ColorUtils
import com.example.myschedule.MainActivity
import com.example.myschedule.R
import com.example.myschedule.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId

object TimerController {

    const val ACTION_TICK = "com.example.myschedule.action.TIMER_TICK"
    const val ACTION_DISMISSED = "com.example.myschedule.action.TIMER_DISMISSED"

    private const val CHANNEL_ID = "lesson_timer"
    private const val NOTIFICATION_ID = 1001
    private const val PREVIEW_NOTIFICATION_ID = 1002

    private const val REQUEST_ALARM = 2001
    private const val REQUEST_DISMISS = 2002
    private const val REQUEST_OPEN_APP = 2003

    private const val RECHECK_HOURS = 12L

    private const val PREVIEW_LENGTH_MS = 5 * 60 * 1000L
    private const val PREVIEW_TIMEOUT_MS = 3 * 60 * 1000L

    private val orange = 0xFFFFA500.toInt()
    private val green = 0xFF66BB6A.toInt()

    private class TimerContent(
        val label: String,
        val title: String,
        val subtitle: String,
        val nextLine: String?,
        val endMillis: Long,
        val accentColor: Int
    )

    suspend fun refresh(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                refreshInternal(context.applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun onDismissed(context: Context) {
        val prefs = NotificationPrefs(context)
        if (prefs.enabled && !prefs.allowSwipeAway) {
            refresh(context)
        }
    }

    private suspend fun refreshInternal(context: Context) {
        val prefs = NotificationPrefs(context)
        val widgetsPresent = hasWidgets(context)

        if (!prefs.enabled && !widgetsPresent) {
            cancelEverything(context)
            return
        }

        val schedule = ScheduleRepository(context).loadSchedule()
        if (schedule == null) {
            cancelEverything(context)
            return
        }

        val now = LocalDateTime.now()
        val options = TimelineOptions(
            preLessonMinutes = prefs.preLessonMinutes,
            includeBreaks = prefs.showBreaks
        )
        val result = TimerTimeline.find(schedule, now, options)

        val segment = result.current
        if (!prefs.enabled || segment == null) {
            cancelNotification(context)
        } else {
            val notification = buildNotification(
                context = context,
                content = contentFor(segment, prefs.showNextLesson),
                pinned = !prefs.allowSwipeAway,
                isPreview = false
            )
            notificationManager(context).notify(NOTIFICATION_ID, notification)
        }

        val widgetOptions = TimelineOptions(preLessonMinutes = 24 * 60, includeBreaks = true)
        val widgetNext = if (widgetsPresent) TimerTimeline.find(schedule, now, widgetOptions).nextChange else null
        val next = listOfNotNull(if (prefs.enabled) result.nextChange else null, widgetNext).minOrNull()
        scheduleAlarm(context, next ?: now.plusHours(RECHECK_HOURS))

        if (widgetsPresent) updateWidgets(context)
    }

    private fun hasWidgets(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        return listOf(ScheduleWidgetReceiver::class.java, ScheduleListWidgetReceiver::class.java).any {
            manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty()
        }
    }

    private suspend fun updateWidgets(context: Context) {
        try {
            ScheduleWidget().updateAll(context)
            ScheduleListWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun contentFor(segment: TimerSegment, showNext: Boolean): TimerContent {
        val lesson = segment.lesson
        val endMillis = segment.end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return when (segment.kind) {
            SegmentKind.LESSON -> {
                val next = segment.nextLesson
                TimerContent(
                    label = "ИДЁТ ЗАНЯТИЕ",
                    title = lesson.name,
                    subtitle = joinParts(lesson.room, "до ${lesson.endTime}"),
                    nextLine = if (showNext && next != null) {
                        joinParts("Далее: ${next.name}", next.startTime, next.room)
                    } else null,
                    endMillis = endMillis,
                    accentColor = lesson.color?.toInt() ?: orange
                )
            }

            SegmentKind.BREAK -> TimerContent(
                label = if (segment.isBeforeFirstLesson) "СКОРО ПЕРВОЕ ЗАНЯТИЕ" else "ПЕРЕМЕНА",
                title = lesson.name,
                subtitle = joinParts("начало в ${lesson.startTime}", lesson.room),
                nextLine = null,
                endMillis = endMillis,
                accentColor = green
            )
        }
    }

    private fun joinParts(vararg parts: String): String =
        parts.filter { it.isNotBlank() }.joinToString(" · ")

    private fun buildNotification(
        context: Context,
        content: TimerContent,
        pinned: Boolean,
        isPreview: Boolean
    ): Notification {
        ensureChannel(context)

        val millisLeft = (content.endMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val chronometerBase = SystemClock.elapsedRealtime() + millisLeft

        val dark = isDarkCard(context)

        val collapsed = RemoteViews(context.packageName, R.layout.notification_timer_collapsed)
        bindCommon(collapsed, content, chronometerBase, dark)

        val expanded = RemoteViews(context.packageName, R.layout.notification_timer_expanded)
        bindCommon(expanded, content, chronometerBase, dark)
        expanded.setTextViewText(R.id.timer_subtitle, content.subtitle)
        expanded.setTextColor(R.id.timer_subtitle, secondaryText(dark))
        expanded.setTextColor(R.id.timer_next, secondaryText(dark))
        if (content.nextLine != null) {
            expanded.setTextViewText(R.id.timer_next, content.nextLine)
            expanded.setViewVisibility(R.id.timer_next, android.view.View.VISIBLE)
        } else {
            expanded.setViewVisibility(R.id.timer_next, android.view.View.GONE)
        }

        val openApp = PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setColor(content.accentColor)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setContentIntent(openApp)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setOngoing(pinned)

        if (pinned && !isPreview) {
            builder.setDeleteIntent(
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_DISMISS,
                    Intent(context, TimerReceiver::class.java).setAction(ACTION_DISMISSED),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        if (isPreview) {
            builder.setTimeoutAfter(PREVIEW_TIMEOUT_MS)
        } else {
            builder.setTimeoutAfter(millisLeft.coerceAtLeast(1000L))
        }

        return builder.build()
    }

    private fun bindCommon(
        views: RemoteViews,
        content: TimerContent,
        chronometerBase: Long,
        dark: Boolean
    ) {
        views.setInt(
            R.id.timer_root, "setBackgroundResource",
            if (dark) R.drawable.notification_card_dark else R.drawable.notification_card_light
        )
        views.setTextColor(R.id.timer_title, if (dark) 0xFFFFFFFF.toInt() else 0xFF1C1B1F.toInt())
        views.setTextColor(R.id.timer_label, secondaryText(dark))

        views.setTextViewText(R.id.timer_label, content.label)
        views.setTextViewText(R.id.timer_title, content.title)

        views.setChronometerCountDown(R.id.timer_chrono, true)
        views.setChronometer(R.id.timer_chrono, chronometerBase, null, true)
        views.setTextColor(
            R.id.timer_chrono,
            if (dark) content.accentColor
            else ColorUtils.blendARGB(content.accentColor, 0xFF000000.toInt(), 0.35f)
        )

        views.setInt(R.id.timer_accent, "setBackgroundColor", content.accentColor)
    }

    private fun secondaryText(dark: Boolean): Int =
        if (dark) 0xFFB8B8B8.toInt() else 0xFF5F5F5F.toInt()

    private fun isDarkCard(context: Context): Boolean =
        when (NotificationPrefs(context).cardStyle) {
            NotificationPrefs.STYLE_DARK -> true
            NotificationPrefs.STYLE_LIGHT -> false
            else -> {
                val mode = android.content.res.Resources.getSystem().configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                mode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

    private fun ensureChannel(context: Context) {
        val manager = notificationManager(context)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Таймер занятия",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Сколько осталось до конца занятия или до его начала"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    private fun notificationManager(context: Context): NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    private fun cancelNotification(context: Context) {
        notificationManager(context).cancel(NOTIFICATION_ID)
    }

    private fun cancelEverything(context: Context) {
        cancelNotification(context)
        context.getSystemService(AlarmManager::class.java).cancel(alarmIntent(context))
    }

    private fun alarmIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_ALARM,
            Intent(context, TimerReceiver::class.java).setAction(ACTION_TICK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun scheduleAlarm(context: Context, at: LocalDateTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAtMillis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = alarmIntent(context)

        val canUseExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
        try {
            if (canUseExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun showPreview(context: Context) {
        val app = context.applicationContext
        val prefs = NotificationPrefs(app)
        val now = System.currentTimeMillis()
        val endMillis = now + PREVIEW_LENGTH_MS

        val endTime = java.time.Instant.ofEpochMilli(endMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .let { String.format("%02d:%02d", it.hour, it.minute) }

        val content = TimerContent(
            label = "ИДЁТ ЗАНЯТИЕ (ПРИМЕР)",
            title = "Программирование",
            subtitle = joinParts("Комп. класс", "до $endTime"),
            nextLine = if (prefs.showNextLesson) "Далее: Математика · 12:00 · 301" else null,
            endMillis = endMillis,
            accentColor = orange
        )

        val notification = buildNotification(app, content, pinned = false, isPreview = true)
        notificationManager(app).notify(PREVIEW_NOTIFICATION_ID, notification)
    }
}
