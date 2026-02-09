package com.example.myschedule.tile

import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
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
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

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
            val currentTime = now.toLocalTime()
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

            var titleText = "Занятий нет"
            var timeText = "На сегодня всё"
            var roomText = ""
            var progress = 0f
            var endInstant: Instant? = null
            var nextLessonText = "" // Объявляем заранее
            var nextLessonRoom = ""

            if (schedule != null) {
                val currentWeek = schedule.weeks.find { it.weekNumber == weekNum }
                val day = currentWeek?.days?.getOrNull(currentDayIndex)

                if (day != null && day.lessons.isNotEmpty()) {
                    val sortedLessons = day.lessons.sortedBy { it.startTime }

                    val currentLesson = sortedLessons.find { lesson ->
                        val start = TimeUtils.parse(lesson.startTime)
                        val end = TimeUtils.parse(lesson.endTime)
                        !currentTime.isBefore(start) && currentTime.isBefore(end)
                    }

                    if (currentLesson != null) {
                        titleText = currentLesson.name
                        timeText = "${currentLesson.startTime} - ${currentLesson.endTime}"
                        roomText = "Каб. ${currentLesson.room}"

                        // Ищем следующую пару
                        val nextLessonObj = sortedLessons.find {
                            TimeUtils.parse(it.startTime).isAfter(TimeUtils.parse(currentLesson.endTime))
                        }
                        if (nextLessonObj != null) {
                            nextLessonText = "Далее: ${nextLessonObj.name}"
                            nextLessonRoom = "Каб. ${nextLessonObj.room}"
                        }

                        val start = TimeUtils.parse(currentLesson.startTime)
                        val end = TimeUtils.parse(currentLesson.endTime)
                        val total = Duration.between(start, end).seconds.toFloat()
                        val passed = Duration.between(start, currentTime).seconds.toFloat()
                        progress = (passed / total).coerceIn(0f, 1f)

                        endInstant = LocalDateTime.of(LocalDate.now(), end)
                            .atZone(ZoneId.systemDefault()).toInstant()

                    } else {
                        val nextLesson = sortedLessons.find { lesson ->
                            val start = TimeUtils.parse(lesson.startTime)
                            currentTime.isBefore(start)
                        }

                        if (nextLesson != null) {
                            titleText = "Далее: ${nextLesson.name}"
                            timeText = "Начало в ${nextLesson.startTime}"
                            roomText = nextLesson.room
                            progress = 0f

                            val start = TimeUtils.parse(nextLesson.startTime)
                            endInstant = LocalDateTime.of(LocalDate.now(), start)
                                .atZone(ZoneId.systemDefault()).toInstant()
                        }
                    }
                }
            }

            TileBuilders.Tile.Builder()
                .setResourcesVersion("1")
                .setFreshnessIntervalMillis(60000)
                .setTileTimeline(
                    TimelineBuilders.Timeline.Builder()
                        .addTimelineEntry(
                            TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(
                                    LayoutElementBuilders.Layout.Builder()
                                        .setRoot(
                                            layout(titleText, timeText, roomText, progress, endInstant, nextLessonText,nextLessonRoom)
                                        )
                                        .build()
                                ).build()
                        ).build()
                ).build()
        }
    }

    // Функция создания таймера (твоя, рабочая)
    private fun buildCountdownTimer(endInstant: Instant): LayoutElementBuilders.LayoutElement {
        val remaining = Duration.between(Instant.now(), endInstant).coerceAtLeast(Duration.ZERO)
        val h = remaining.toHours()
        val m = (remaining.toMinutes() % 60)
        val s = (remaining.seconds % 60)
        val fallback = String.format("%d:%02d:%02d", h, m, s)

        val duration = DynamicBuilders.DynamicInstant.platformTimeWithSecondsPrecision()
            .durationUntil(
                DynamicBuilders.DynamicInstant.withSecondsPrecision(endInstant)
            )

        val totalSec = duration.toIntSeconds()

        val hours = totalSec.div(3600)
        val minutes = totalSec.rem(3600).div(60)
        val seconds = totalSec.rem(60)

        val minutesStr = DynamicBuilders.DynamicString.onCondition(minutes.lt(10))
            .use(DynamicBuilders.DynamicString.constant("0").concat(minutes.format()))
            .elseUse(minutes.format())

        val secondsStr = DynamicBuilders.DynamicString.onCondition(seconds.lt(10))
            .use(DynamicBuilders.DynamicString.constant("0").concat(seconds.format()))
            .elseUse(seconds.format())

        // Формат H:MM:SS
        val dynamicText = hours.format()
            .concat(DynamicBuilders.DynamicString.constant(":"))
            .concat(minutesStr)
            .concat(DynamicBuilders.DynamicString.constant(":"))
            .concat(secondsStr)

        return LayoutElementBuilders.Text.Builder()
            .setText(
                TypeBuilders.StringProp.Builder(fallback)
                    .setDynamicValue(dynamicText)
                    .build()
            )
            .setLayoutConstraintsForDynamicText(
                TypeBuilders.StringLayoutConstraint.Builder("88:88:88")
                    .setAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
                    .build()
            )
            .setFontStyle(
                LayoutElementBuilders.FontStyle.Builder()
                    .setSize(sp(22f)) // Крупный таймер
                    .setColor(argb(0xFFFFA500.toInt())) // Оранжевый
                    .build()
            )
            .build()
    }

    private fun layout(
        title: String,
        time: String,
        room: String,
        progress: Float,
        endInstant: Instant?,
        nextLessonText: String,
        nextLessonRoom: String
    ): LayoutElementBuilders.LayoutElement {

        val timeElement = if (endInstant != null && endInstant.isAfter(Instant.now())) {
            buildCountdownTimer(endInstant)
        } else {
            LayoutElementBuilders.Text.Builder()
                .setText(time)
                .setFontStyle(
                    LayoutElementBuilders.FontStyle.Builder()
                        .setSize(sp(14f))
                        .setColor(argb(0xFFAAAAAA.toInt()))
                        .build()
                )
                .build()
        }

        val columnBuilder = LayoutElementBuilders.Column.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)

            // ═══ Название предмета (2 строки, с троеточием) ═══
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(title)
                    .setMaxLines(2)
                    .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
                    .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(12f))
                            .setColor(argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setStart(dp(31f))
                                    .setEnd(dp(31f))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )

            // Отступ между названием и кабинетом
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(3f)).build())

            // ═══ Кабинет ═══
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(room)
                    .setMaxLines(1)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(10f))
                            .setColor(argb(0xFFFFA500.toInt()))
                            .build()
                    )
                    .build()
            )

            // Отступ перед таймером
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(2f)).build())

            // ═══ ТАЙМЕР ═══
            .addContent(timeElement)

            // Отступ после таймера
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(3f)).build())

        // ═══ Следующий предмет (если есть) ═══
        if (nextLessonText.isNotEmpty()) {
            // Разделитель
            columnBuilder.addContent(
                LayoutElementBuilders.Box.Builder()
                    .setWidth(dp(50f))
                    .setHeight(dp(1f))
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setBackground(
                                ModifiersBuilders.Background.Builder()
                                    .setColor(argb(0xFF444444.toInt()))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )

            columnBuilder.addContent(
                LayoutElementBuilders.Spacer.Builder().setHeight(dp(3f)).build()
            )

            // Текст "Далее:" — название
            columnBuilder.addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(nextLessonText)
                    .setMaxLines(2)
                    .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
                    .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(9f))
                            .setColor(argb(0xFF888888.toInt()))
                            .build()
                    )
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setStart(dp(30f))
                                    .setEnd(dp(30f))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
                // Отступ между названием и кабинетом
                .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(1f)).build())

                // ═══ Кабинет ═══
                .addContent(
                    LayoutElementBuilders.Text.Builder()
                        .setText(nextLessonRoom)
                        .setMaxLines(1)
                        .setFontStyle(
                            LayoutElementBuilders.FontStyle.Builder()
                                .setSize(sp(9f))
                                .setColor(argb(0xFF888888.toInt()))
                                .build()
                        )
                        .build()
                )
        }

        val contentColumn = columnBuilder.build()

        val progressBar = CircularProgressIndicator.Builder()
            .setProgress(progress)
            .setStartAngle(0f)
            .setEndAngle(360f)
            .setStrokeWidth(dp(6f))
            .setCircularProgressIndicatorColors(
                ProgressIndicatorColors(
                    argb(0xFFFFA500.toInt()),
                    argb(0xFF333333.toInt())
                )
            )
            .build()

        return LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(progressBar)
            // Контент с отступами от кругового прогресса
            .addContent(
                LayoutElementBuilders.Box.Builder()
                    .setWidth(expand())
                    .setHeight(wrap())
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setTop(dp(16f))
                                    .setBottom(dp(16f))
                                    .build()
                            )
                            .build()
                    )
                    .addContent(contentColumn)
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