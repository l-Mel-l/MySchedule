package com.example.myschedule.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.myschedule.Lesson
import com.example.myschedule.MainActivity

class ScheduleWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = loadWidgetSnapshot(context)
        val palette = widgetPalette(context)

        provideContent {
            WidgetContent(snapshot, palette)
        }
    }

    @Composable
    private fun WidgetContent(snap: WidgetSnapshot, p: WidgetPalette) {
        val compact = LocalSize.current.height < 110.dp

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(p.background)
                .cornerRadius(18.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            when (snap.kind) {
                WidgetKind.LESSON -> {
                    val lesson = snap.lesson!!
                    val sub = listOf(lesson.room, "до ${lesson.endTime}").filter { it.isNotBlank() }.joinToString(" · ")
                    val next = snap.nextLesson?.let { nextLine(it) }
                    val color = lessonColor(lesson, p)
                    TimerLayout(
                        compact, p, "ИДЁТ ЗАНЯТИЕ", lesson.name, sub, next,
                        snap.endMillis, color, lessonBar(lesson, p)
                    )
                }
                WidgetKind.BREAK -> {
                    val lesson = snap.lesson!!
                    val sub = listOf("начало в ${lesson.startTime}", lesson.room).filter { it.isNotBlank() }.joinToString(" · ")
                    val label = if (snap.beforeFirst) "СЛЕДУЮЩЕЕ ЗАНЯТИЕ" else "ПЕРЕМЕНА"
                    TimerLayout(
                        compact, p, label, lesson.name, sub, null,
                        snap.endMillis, p.accentArgb, p.accent
                    )
                }
                WidgetKind.FINISHED -> Message(p, "ЗАНЯТИЯ ОКОНЧЕНЫ", "На сегодня всё!")
                WidgetKind.FREE -> Message(p, "СЕГОДНЯ ЗАНЯТИЙ НЕТ", "Отдыхайте")
            }
        }
    }

    @Composable
    private fun TimerLayout(
        compact: Boolean,
        p: WidgetPalette,
        label: String,
        title: String,
        subtitle: String,
        next: String?,
        endMillis: Long,
        timerColor: Int,
        bar: androidx.glance.unit.ColorProvider
    ) {
        if (compact) {
            Box(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(end = 112.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = GlanceModifier.width(4.dp).height(42.dp).background(bar)) {}
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    Column {
                        Text(label, style = TextStyle(color = p.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                        Text(title, style = TextStyle(color = p.text, fontSize = 15.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                        Text(subtitle, style = TextStyle(color = p.textSecondary, fontSize = 12.sp), maxLines = 1)
                    }
                }
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    WidgetTimerText(endMillis, timerColor, large = false)
                }
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = GlanceModifier.width(4.dp).fillMaxHeight().background(bar)) {}
                Spacer(modifier = GlanceModifier.width(12.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(label, style = TextStyle(color = p.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                    WidgetTimerText(endMillis, timerColor, large = true)
                    Text(title, style = TextStyle(color = p.text, fontSize = 17.sp, fontWeight = FontWeight.Bold), maxLines = 2)
                    Text(subtitle, style = TextStyle(color = p.textSecondary, fontSize = 13.sp), maxLines = 1)
                    if (next != null) {
                        Spacer(modifier = GlanceModifier.height(6.dp))
                        Text(next, style = TextStyle(color = p.textSecondary, fontSize = 12.sp), maxLines = 2)
                    }
                }
            }
        }
    }

    @Composable
    private fun Message(p: WidgetPalette, title: String, subtitle: String) {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = TextStyle(color = p.accent, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                subtitle,
                style = TextStyle(color = p.textSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
            )
        }
    }

    private fun nextLine(next: Lesson): String =
        listOf("Далее: ${next.name}", next.startTime, next.room).filter { it.isNotBlank() }.joinToString(" · ")

    private fun lessonColor(lesson: Lesson, p: WidgetPalette): Int =
        lesson.color?.toInt() ?: p.accentArgb

    private fun lessonBar(lesson: Lesson, p: WidgetPalette): androidx.glance.unit.ColorProvider =
        lesson.color?.let { fixedColor(Color(it)) } ?: p.accent
}
