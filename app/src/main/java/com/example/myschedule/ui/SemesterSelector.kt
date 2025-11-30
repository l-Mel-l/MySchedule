package com.example.myschedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SemesterSelector(
    selectedWeek: Int,
    currentRealWeek: Int, // Реальная неделя по календарю
    onWeekSelected: (Int) -> Unit
) {
    // Состояние списка, чтобы мы могли прокрутить его к выбранной неделе
    val listState = rememberLazyListState()

    // Прокручиваем к выбранной неделе при старте
    LaunchedEffect(selectedWeek) {
        // -2 чтобы выбранная неделя была не с самого края, а чуть поцентре
        val indexToScroll = (selectedWeek - 2).coerceAtLeast(0)
        listState.animateScrollToItem(indexToScroll)
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Генерируем 24 недели (обычно в семестре 18-20, возьмем с запасом)
        items(24) { index ->
            val weekNum = index + 1
            SemesterWeekItem(
                number = weekNum,
                isSelected = weekNum == selectedWeek,
                isCurrentReal = weekNum == currentRealWeek,
                onClick = { onWeekSelected(weekNum) }
            )
        }
    }
}

@Composable
fun SemesterWeekItem(
    number: Int,
    isSelected: Boolean,
    isCurrentReal: Boolean,
    onClick: () -> Unit
) {
    // Цвета
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val surface = MaterialTheme.colorScheme.surface

    val backgroundColor = if (isSelected) primary else Color.Transparent

    // Обводка: Если это текущая неделя - жирная оранжевая. Если нет - тонкая серая или нет вообще.
    val borderColor = when {
        isSelected -> Color.Transparent // У выбранной заливка, обводка не нужна
        isCurrentReal -> primary        // У текущей - оранжевая обводка
        else -> Color.Gray.copy(alpha = 0.3f) // У остальных - еле заметная
    }

    val textColor = if (isSelected) onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(44.dp) // Размер кружочка
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isCurrentReal && !isSelected) 2.dp else 1.dp, // Текущую выделяем жирнее
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected || isCurrentReal) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}