package com.example.myschedule.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.myschedule.Lesson
import com.example.myschedule.MainActivity
import com.example.myschedule.TimeUtils

class ScheduleListWidget : GlanceAppWidget() {

    private enum class Status { PAST, CURRENT, UPCOMING }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = loadWidgetSnapshot(context)
        val palette = widgetPalette(context)

        provideContent {
            ListWidgetContent(snapshot, palette)
        }
    }

    @Composable
    private fun ListWidgetContent(snap: WidgetSnapshot, p: WidgetPalette) {
        val open = actionStartActivity<MainActivity>()
        val footer = footerText(snap)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(p.background)
                .cornerRadius(18.dp)
                .clickable(open)
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Заголовок
                Text(
                    text = snap.dayTitle,
                    style = TextStyle(color = p.accent, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    maxLines = 1,
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )

                if (snap.lessons.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Занятий нет", style = TextStyle(color = p.textSecondary, textAlign = TextAlign.Center))
                    }
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        items(snap.lessons) { lesson ->
                            Column {
                                LessonItemRow(lesson, statusOf(snap, lesson), p)
                                Spacer(modifier = GlanceModifier.height(6.dp))
                            }
                        }
                    }
                }

                if (footer != null) {
                    Text(
                        text = footer,
                        style = TextStyle(color = p.textSecondary, fontSize = 12.sp),
                        maxLines = 2,
                        modifier = GlanceModifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    private fun statusOf(snap: WidgetSnapshot, lesson: Lesson): Status {
        if (snap.kind == WidgetKind.LESSON && snap.lesson?.id == lesson.id) return Status.CURRENT
        val end = TimeUtils.parse(lesson.endTime)
        return if (!snap.now.toLocalTime().isBefore(end)) Status.PAST else Status.UPCOMING
    }

    private fun footerText(snap: WidgetSnapshot): String? = when (snap.kind) {
        WidgetKind.LESSON -> snap.nextLesson?.let {
            listOf("Далее: ${it.name}", it.startTime, it.room).filter { part -> part.isNotBlank() }.joinToString(" · ")
        } ?: "Это последнее занятие на сегодня"
        WidgetKind.BREAK -> snap.lesson?.let { "Следующее занятие в ${it.startTime}" }
        WidgetKind.FINISHED -> "Занятия на сегодня закончились"
        WidgetKind.FREE -> null
    }

    @Composable
    private fun LessonItemRow(lesson: Lesson, status: Status, p: WidgetPalette) {
        val rowBg = if (status == Status.CURRENT) p.activeCard else p.card
        val main = when (status) {
            Status.CURRENT -> p.activeText
            Status.PAST -> p.cardTextSecondary
            Status.UPCOMING -> p.cardText
        }
        val secondary = if (status == Status.CURRENT) p.activeTextSecondary else p.cardTextSecondary
        val barColor = lesson.color?.let { fixedColor(Color(it)) } ?: p.accent

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(rowBg)
                .cornerRadius(12.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Время
            Column(modifier = GlanceModifier.width(50.dp)) {
                Text(lesson.startTime, style = TextStyle(color = main, fontWeight = FontWeight.Bold))
                Text(lesson.endTime, style = TextStyle(color = secondary, fontSize = 12.sp))
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Цветная палочка
            Box(modifier = GlanceModifier.width(4.dp).height(30.dp).background(barColor)) {}

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Название и кабинет
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(lesson.name, style = TextStyle(color = main), maxLines = 1)
                if (lesson.room.isNotEmpty()) {
                    Text(lesson.room, style = TextStyle(color = secondary, fontSize = 12.sp))
                }
            }

            if (status == Status.CURRENT) {
                Text(
                    "идёт",
                    style = TextStyle(color = p.activeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
