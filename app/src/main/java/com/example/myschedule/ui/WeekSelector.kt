package com.example.myschedule.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WeekSelector(
    selectedWeek: Int,
    currentRealWeek: Int,
    onWeekSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WeekButton(
            text = "Неделя 1",
            isSelected = selectedWeek == 1,
            isCurrentReal = currentRealWeek == 1,
            onClick = { onWeekSelected(1) },
            modifier = Modifier.weight(1f)
        )

        WeekButton(
            text = "Неделя 2",
            isSelected = selectedWeek == 2,
            isCurrentReal = currentRealWeek == 2,
            onClick = { onWeekSelected(2) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WeekButton(
    text: String,
    isSelected: Boolean,
    isCurrentReal: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(300),
        label = "colorAnim"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray,
        animationSpec = tween(300),
        label = "textAnim"
    )

    // Расчет цвета рамки
    val borderColor = when {
        isSelected -> Color.Transparent
        isCurrentReal -> MaterialTheme.colorScheme.primary // Оранжевая рамка для текущей
        else -> Color.Gray.copy(alpha = 0.5f)
    }

    // Расчет толщины рамки
    val borderWidth = if (isCurrentReal && !isSelected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            // ИСПОЛЬЗУЕМ НАШИ ПЕРЕМЕННЫЕ ЗДЕСЬ:
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}