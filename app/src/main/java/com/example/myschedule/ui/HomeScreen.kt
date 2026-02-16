package com.example.myschedule.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myschedule.*
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.alpha

@Composable
fun HomeScreen(
    viewModel: ScheduleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentState by remember { mutableStateOf<StudentState>(StudentState.Loading) }


    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.schedule, uiState.selectedWeekNumber) {
        while (true) {
            val now = java.time.LocalDateTime.now()

            val currentDayOfWeek = now.dayOfWeek.value - 1

            val todayIndex = currentDayOfWeek

            val schedule = uiState.schedule
            if (schedule != null) {
                val week = schedule.weeks.find { it.weekNumber == uiState.selectedWeekNumber }
                    ?: schedule.weeks[0]
                val day = week.days.getOrNull(todayIndex)

                if (day == null || day.lessons.isEmpty()) {
                    currentState = StudentState.FreeDay
                } else {
                    currentState = calculateState(day.lessons)
                }
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Текущее состояние",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center) {

            if (currentState is StudentState.Loading) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(280.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp
                )
            }

            else if (currentState is StudentState.DayFinished || currentState is StudentState.FreeDay) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(280.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    strokeWidth = 12.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Weekend,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (currentState is StudentState.FreeDay) "Выходной" else "Занятия окончены",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

            } else {
                val targetProgress = when (val s = currentState) {
                    is StudentState.LessonNow -> s.progress
                    is StudentState.BreakNow -> s.progress
                    else -> 0f
                }

                val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
                    label = "TimerAnimation"
                )

                // Фон
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(280.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp
                )

                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.size(280.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )

                // Текст таймера
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val timerText = when (val s = currentState) {
                        is StudentState.LessonNow -> TimeUtils.formatRemaining(s.secondsLeft)
                        is StudentState.BreakNow -> TimeUtils.formatRemaining(s.secondsLeft)
                        else -> ""
                    }

                    val labelText = when (currentState) {
                        is StudentState.LessonNow -> "До конца занятия"
                        is StudentState.BreakNow -> "До начала занятия"
                        else -> ""
                    }

                    Text(text = labelText, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = timerText,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when(val s = currentState) {
            is StudentState.LessonNow -> {
                Text("Идет занятие:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LessonItem(lesson = s.lesson, onLongClick = {}, onClick = {})

                if (s.nextLesson != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Далее:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LessonItem(
                        lesson = s.nextLesson,
                        onLongClick = {},
                        onClick = {},
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }
            is StudentState.BreakNow -> {
                Text("Следующее занятие:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LessonItem(lesson = s.nextLesson, onLongClick = {},onClick = {})
            }
            else -> {
                Text("Отдыхайте", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

fun calculateState(lessons: List<Lesson>): StudentState {
    val now = TimeUtils.now()
    val sortedLessons = lessons.sortedBy { it.startTime }

    for ((index, lesson) in sortedLessons.withIndex()) {
        val start = TimeUtils.parse(lesson.startTime)
        val end = TimeUtils.parse(lesson.endTime)

        if (now.isAfter(start) && now.isBefore(end)) {
            val totalSeconds = TimeUtils.secondsBetween(start, end)
            val passedSeconds = TimeUtils.secondsBetween(start, now)
            val remaining = totalSeconds - passedSeconds
            val progress = passedSeconds.toFloat() / totalSeconds.toFloat()

            val nextLesson = sortedLessons.getOrNull(index + 1)

            return StudentState.LessonNow(lesson, nextLesson, progress, remaining)
        }

        if (now.isBefore(start)) {
            val remaining = TimeUtils.secondsBetween(now, start)

            var progress = 0f
            if (index > 0) {
                val prevLessonEnd = TimeUtils.parse(sortedLessons[index - 1].endTime)
                val totalBreakSeconds = TimeUtils.secondsBetween(prevLessonEnd, start)
                val passedBreakSeconds = TimeUtils.secondsBetween(prevLessonEnd, now)

                if (totalBreakSeconds > 0) {
                    progress = passedBreakSeconds.toFloat() / totalBreakSeconds.toFloat()
                }
            } else {
                progress = 0f
            }

            return StudentState.BreakNow(lesson, progress, remaining)
        }
    }

    return StudentState.DayFinished()
}