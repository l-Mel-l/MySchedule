package com.example.myschedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DaySelector(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit, // Функция, которая сработает при нажатии
    modifier: Modifier = Modifier
) {
    // Короткие названия дней
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, // Равномерно распределить по ширине
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(days) { index, dayName ->
            DayChip(
                text = dayName,
                isSelected = index == selectedDayIndex,
                onClick = { onDaySelected(index) }
            )
        }
    }
}

@Composable
fun DayChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp) // Размер кружочка
            .clip(CircleShape) // Делаем его круглым
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary // Оранжевый, если выбран
                else Color.Transparent // Прозрачный, если не выбран
            )
            .clickable { onClick() }, // Обработка нажатия
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray
        )
    }
}