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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.*
import com.example.myschedule.ScheduleType
import com.example.myschedule.TimeUtils
import com.example.myschedule.WearScheduleRepository
import com.example.myschedule.presentation.theme.MyScheduleTheme
import java.time.LocalDateTime

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

@Composable
fun WearScheduleApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { WearScheduleRepository(context) }

    var todaysLessons by remember { mutableStateOf<List<com.example.myschedule.Lesson>>(emptyList()) }
    var dayTitle by remember { mutableStateOf("Загрузка...") }

    // Загружаем данные при старте
    LaunchedEffect(Unit) {
        val schedule = repository.loadSchedule()
        val now = LocalDateTime.now()
        val currentDayIndex = now.dayOfWeek.value - 1

        if (schedule != null) {
            val settings = schedule.settings
            val weekNum = when (settings.scheduleType) {
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

            val currentWeek = schedule.weeks.find { it.weekNumber == weekNum }
            val day = currentWeek?.days?.getOrNull(currentDayIndex)

            if (day != null) {
                todaysLessons = day.lessons.sortedBy { it.startTime }
                dayTitle = day.dayName
            } else {
                dayTitle = "Сегодня пусто"
            }
        } else {
            dayTitle = "Нет данных"
        }
    }

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        timeText = {
            TimeText()
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
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
            ListHeader {
                Text(text = dayTitle, color = MaterialTheme.colors.primary)
            }
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
            items(todaysLessons.size) { index ->
                val lesson = todaysLessons[index]

                Card(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundPainter = CardDefaults.cardBackgroundPainter(
                        startBackgroundColor = Color(0xFF1E1E1E),
                        endBackgroundColor = Color(0xFF1E1E1E)
                    ),
                    contentColor = Color.White
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Время
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = lesson.startTime,
                                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lesson.endTime,
                                style = MaterialTheme.typography.caption2.copy(color = Color.Gray)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Оранжевая полоска
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(Color(lesson.color ?: 0xFFFFA500), RoundedCornerShape(50))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Название и кабинет
                        Column {
                            Text(
                                text = lesson.name,
                                maxLines = 2,
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
        }
    }
    }
}