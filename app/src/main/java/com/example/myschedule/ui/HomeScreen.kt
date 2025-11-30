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

    // Чтобы экран можно было прокрутить, если список занятий длинный
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.schedule, uiState.selectedWeekNumber) {
        while (true) {
            val now = java.time.LocalDateTime.now()

            // 1. Какой сегодня день недели? (0 = Понедельник, 6 = Воскресенье)
            val currentDayOfWeek = now.dayOfWeek.value - 1
            // Таймер будет работать для ТОЙ недели, которую ты выбрал вверху.
            // Иначе нам нужно настройки даты начала семестра делать.
            // НО день недели должен быть реальным!

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
            .verticalScroll(scrollState) // Разрешаем скролл
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Текущее состояние",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- ТАЙМЕР ИЛИ СТАТУС "ОКОНЧЕНО" ---
        Box(contentAlignment = Alignment.Center) {

            if (currentState is StudentState.Loading) {
                CircularProgressIndicator(
                    progress = 1f, // Полный круг
                    modifier = Modifier.size(280.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant, // Серый цвет
                    strokeWidth = 12.dp
                )
                // Текст не пишем, пусть будет пусто мгновение
            }

            else if (currentState is StudentState.DayFinished || currentState is StudentState.FreeDay) {
                // ВАРИАНТ 1: ДЕНЬ ЗАКОНЧЕН / ВЫХОДНОЙ
                // Рисуем просто красивый заполненный круг (статичный)
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(280.dp),
                    color = MaterialTheme.colorScheme.primaryContainer, // Мягкий цвет (не такой яркий)
                    strokeWidth = 12.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Большая иконка вместо цифр
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Weekend, // Диван/Выходной (или замени на Star/Home)
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
                // ВАРИАНТ 2: ИДЕТ ОТСЧЕТ (ПАРА ИЛИ ПЕРЕМЕНА)
                val targetProgress = when (val s = currentState) {
                    is StudentState.LessonNow -> s.progress
                    is StudentState.BreakNow -> s.progress
                    else -> 0f
                }

                // Плавная анимация заполнения круга
                val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 500), // полсекунды плавности
                    label = "TimerAnimation"
                )

                // Фон
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(280.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp
                )

                // Активный прогресс (АНИМИРОВАННЫЙ)
                CircularProgressIndicator(
                    progress = animatedProgress, // Используем плавное значение
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

        // --- КАРТОЧКИ ---
        when(val s = currentState) {
            is StudentState.LessonNow -> {
                Text("Идет занятие:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LessonItem(lesson = s.lesson, onLongClick = {}, onClick = {})

                // Если есть следующая пара - показываем её
                if (s.nextLesson != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Далее:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Делаем карточку чуть прозрачной, чтобы показать, что она следующая
                    LessonItem(
                        lesson = s.nextLesson,
                        onLongClick = {},
                        onClick = {},
                        modifier = Modifier.alpha(0.7f) // Прозрачность
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

// --- ОБНОВЛЕННАЯ ЛОГИКА РАСЧЕТА ---
fun calculateState(lessons: List<Lesson>): StudentState {
    val now = TimeUtils.now()
    val sortedLessons = lessons.sortedBy { it.startTime }

    for ((index, lesson) in sortedLessons.withIndex()) {
        val start = TimeUtils.parse(lesson.startTime)
        val end = TimeUtils.parse(lesson.endTime)

        // 1. ИДЕТ ПАРА
        if (now.isAfter(start) && now.isBefore(end)) {
            val totalSeconds = TimeUtils.secondsBetween(start, end)
            val passedSeconds = TimeUtils.secondsBetween(start, now)
            val remaining = totalSeconds - passedSeconds
            val progress = passedSeconds.toFloat() / totalSeconds.toFloat()

            // Ищем следующую пару
            val nextLesson = sortedLessons.getOrNull(index + 1)

            return StudentState.LessonNow(lesson, nextLesson, progress, remaining)
        }

        // 2. ПЕРЕМЕНА (Мы ПЕРЕД этой парой)
        if (now.isBefore(start)) {
            val remaining = TimeUtils.secondsBetween(now, start)

            // Расчет прогресса перемены
            var progress = 0f
            if (index > 0) {
                // Если это не первая пара, то перемена началась, когда кончилась предыдущая
                val prevLessonEnd = TimeUtils.parse(sortedLessons[index - 1].endTime)
                val totalBreakSeconds = TimeUtils.secondsBetween(prevLessonEnd, start)
                val passedBreakSeconds = TimeUtils.secondsBetween(prevLessonEnd, now)

                if (totalBreakSeconds > 0) {
                    progress = passedBreakSeconds.toFloat() / totalBreakSeconds.toFloat()
                }
            } else {
                // Если это самое утро (до первой пары)
                // Пусть круг будет полным или пустым. Давай сделаем пустым (0f), типа "еще не началось"
                progress = 0f
            }

            return StudentState.BreakNow(lesson, progress, remaining)
        }
    }

    return StudentState.DayFinished()
}