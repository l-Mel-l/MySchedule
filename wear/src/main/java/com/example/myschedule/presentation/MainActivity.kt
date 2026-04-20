package com.example.myschedule.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.*
import com.example.myschedule.*
import com.example.myschedule.presentation.theme.MyScheduleTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MyScheduleTheme {
                WearScheduleApp()
            }
        }
    }
}

private val dayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
private val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

@Composable
fun WearScheduleApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { WearScheduleRepository(context) }

    var schedule by remember { mutableStateOf<MainSchedule?>(null) }
    var dayOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        schedule = repository.loadSchedule()
    }

    val dayData = remember(dayOffset, schedule) {
        val sched = schedule ?: return@remember null
        
        val targetDate = LocalDate.now().plusDays(dayOffset.toLong())
        val dayIndex = targetDate.dayOfWeek.value - 1

        val settings = sched.settings
        val weekNum = when (settings.scheduleType) {
            ScheduleType.Fixed -> 1
            ScheduleType.Rotation -> {
                val start = settings.semesterStartDate
                if (start != null) {
                    val abs = TimeUtils.getWeekNumberForDate(start, targetDate)
                    if (abs % 2 != 0) 1 else 2
                } else 1
            }
            ScheduleType.Semester -> {
                val start = settings.semesterStartDate
                if (start != null) TimeUtils.getWeekNumberForDate(start, targetDate) else 1
            }
        }

        val currentWeek = sched.weeks.find { it.weekNumber == weekNum }
        val day = currentWeek?.days?.getOrNull(dayIndex)

        val lessons = day?.lessons?.sortedBy { it.startTime } ?: emptyList()
        val title = day?.dayName ?: dayNames.getOrElse(dayIndex) { "" }
        val subtitle = when (dayOffset) {
            0 -> "Сегодня"
            1 -> "Завтра"
            -1 -> "Вчера"
            else -> targetDate.format(dateFormatter)
        }

        Triple(title, subtitle, lessons)
    }

    val dayTitle = dayData?.first ?: "Нет данных"
    val dateSubtitle = dayData?.second ?: ""
    val todaysLessons = dayData?.third ?: emptyList()

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    val cardBackground = CardDefaults.cardBackgroundPainter(
        startBackgroundColor = Color(0xFF1E1E1E),
        endBackgroundColor = Color(0xFF1E1E1E)
    )

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .rotaryScrollable(
                    behavior = RotaryScrollableDefaults.behavior(scrollableState = listState),
                    focusRequester = focusRequester
                ),
            state = listState,
            anchorType = ScalingLazyListAnchorType.ItemStart,
            contentPadding = PaddingValues(top = 28.dp, start = 10.dp, end = 10.dp, bottom = 40.dp)
        ) {
            item {
                DayHeader(
                    title = dayTitle,
                    subtitle = dateSubtitle,
                    onPrevDay = { dayOffset-- },
                    onNextDay = { dayOffset++ }
                )
            }

            if (todaysLessons.isEmpty()) {
                item {
                    Text(
                        text = "Занятий нет",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                }
            } else {
                items(
                    items = todaysLessons,
                    key = { it.id }
                ) { lesson ->
                    LessonCard(lesson, cardBackground)
                }
            }
        }
    }
}

@Composable
fun DayHeader(
    title: String,
    subtitle: String,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPrevDay,
            modifier = Modifier.size(32.dp),
            colors = ButtonDefaults.secondaryButtonColors()
        ) {
            Text("<", fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.caption2,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = onNextDay,
            modifier = Modifier.size(32.dp),
            colors = ButtonDefaults.secondaryButtonColors()
        ) {
            Text(">", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson, backgroundPainter: Painter) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        backgroundPainter = backgroundPainter,
        contentColor = Color.White
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = lesson.startTime,
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lesson.endTime,
                    style = MaterialTheme.typography.caption2.copy(color = Color.Gray)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            val markerColor = remember(lesson.color) {
                lesson.color?.let { Color(it) } ?: Color(0xFFFFA500)
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(markerColor, RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = lesson.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                )
                if (lesson.room.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.room,
                        style = MaterialTheme.typography.caption2.copy(color = Color.Gray)
                    )
                }
            }
        }
    }
}
