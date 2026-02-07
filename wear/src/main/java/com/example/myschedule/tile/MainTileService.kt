package com.example.myschedule.tile

import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.DimensionBuilders.wrap // <--- ДОБАВИЛИ
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders // <--- ДЛЯ PADDING
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.myschedule.WearScheduleRepository
import com.example.myschedule.ScheduleType
import com.example.myschedule.TimeUtils
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.future
import java.time.LocalDateTime

class MainTileService : TileService() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var repository: WearScheduleRepository

    override fun onCreate() {
        super.onCreate()
        repository = WearScheduleRepository(this)
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        return scope.future {
            val schedule = repository.loadSchedule()

            val now = LocalDateTime.now()
            val currentDayIndex = now.dayOfWeek.value - 1
            var weekNum = 1

            if (schedule != null) {
                val settings = schedule.settings
                weekNum = when (settings.scheduleType) {
                    ScheduleType.Fixed -> 1
                    ScheduleType.Rotation -> {
                        val start = settings.semesterStartDate
                        if (start != null) {
                            val abs = TimeUtils.getCurrentWeekNumber(start)
                            if (abs % 2 != 0) 1 else 2
                        } else 1
                    }
                    ScheduleType.Semester -> {
                        val start = settings.semesterStartDate
                        if (start != null) TimeUtils.getCurrentWeekNumber(start) else 1
                    }
                }
            }

            // ... (Логи отладки можешь оставить, если нужны) ...

            // --- ПЕРЕМЕННЫЕ ДЛЯ ПЛИТКИ ---
            var titleText = "Занятий нет"
            var timeText = "На сегодня всё"
            var roomText = ""
            var progress = 0f

            if (schedule != null) {
                val currentWeek = schedule.weeks.find { it.weekNumber == weekNum }
                val day = currentWeek?.days?.getOrNull(currentDayIndex)

                if (day != null && day.lessons.isNotEmpty()) {
                    val currentTime = now.toLocalTime()
                    val sortedLessons = day.lessons.sortedBy { it.startTime }

                    val currentLesson = sortedLessons.find { lesson ->
                        val start = TimeUtils.parse(lesson.startTime)
                        val end = TimeUtils.parse(lesson.endTime)
                        !currentTime.isBefore(start) && currentTime.isBefore(end)
                    }

                    if (currentLesson != null) {
                        // ИДЕТ ПАРА
                        titleText = currentLesson.name
                        timeText = "${currentLesson.startTime} - ${currentLesson.endTime}"
                        roomText = currentLesson.room

                        // Считаем прогресс
                        val start = TimeUtils.parse(currentLesson.startTime)
                        val end = TimeUtils.parse(currentLesson.endTime)
                        val totalSeconds = java.time.Duration.between(start, end).seconds.toFloat()
                        val passedSeconds = java.time.Duration.between(start, currentTime).seconds.toFloat()
                        progress = (passedSeconds / totalSeconds).coerceIn(0f, 1f)

                    } else {
                        // ПЕРЕМЕНА
                        val nextLesson = sortedLessons.find { lesson ->
                            val start = TimeUtils.parse(lesson.startTime)
                            currentTime.isBefore(start)
                        }

                        if (nextLesson != null) {
                            titleText = "Далее: ${nextLesson.name}"
                            timeText = "Начало в ${nextLesson.startTime}"
                            roomText = nextLesson.room
                            progress = 0f // Круг пустой, ждем начала
                        }
                    }
                }
            }

            // --- ВЫЗОВ layout С НОВЫМИ ПАРАМЕТРАМИ ---
            TileBuilders.Tile.Builder()
                .setResourcesVersion("1")
                .setTileTimeline(
                    TimelineBuilders.Timeline.Builder()
                        .addTimelineEntry(
                            TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(
                                    LayoutElementBuilders.Layout.Builder()
                                        .setRoot(layout(titleText, timeText, roomText, progress)) // <--- ИСПРАВИЛИ
                                        .build()
                                ).build()
                        ).build()
                ).build()
        }
    }

    private fun layout(
        title: String,
        time: String,
        room: String,
        progress: Float
    ): LayoutElementBuilders.LayoutElement {

        // 1. Текстовая колонка
        val textContent = LayoutElementBuilders.Column.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(title)
                    .setMaxLines(2)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(16f))
                            .setColor(argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(4f)).build())
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(time)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(12f))
                            .setColor(argb(0xFFAAAAAA.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(4f)).build())
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(room)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(14f))
                            .setColor(argb(0xFFFFA500.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()

        // 2. Круговой прогресс
        val progressBar = LayoutElementBuilders.CircularProgressIndicator.Builder()
            .setProgress(progress)
            .setStartAngle(0f)
            .setEndAngle(360f)
            .setStrokeWidth(dp(6f))
            .setColor(argb(0xFFFFA500.toInt()))
            .setTrackColor(argb(0xFF333333.toInt()))
            .build()

        // 3. Сборка
        return LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .addContent(progressBar)
            // Добавляем отступы через setModifiers -> setPadding
            .addContent(
                LayoutElementBuilders.Box.Builder()
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setPadding(ModifiersBuilders.Padding.Builder().setAll(dp(20f)).build())
                            .build()
                    )
                    .addContent(textContent)
                    .build()
            )
            .build()
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<androidx.wear.tiles.ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            androidx.wear.tiles.ResourceBuilders.Resources.Builder()
                .setVersion("1")
                .build()
        )
    }
}