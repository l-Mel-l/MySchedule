package com.example.myschedule.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myschedule.WeekSchedule
import com.example.myschedule.displayName
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val rangeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"))

private fun weekRange(startDate: LocalDate?, weekNumber: Int): String? {
    if (startDate == null) return null
    val monday = startDate
        .minusDays((startDate.dayOfWeek.value - 1).toLong())
        .plusWeeks((weekNumber - 1).toLong())
    return "${monday.format(rangeFormatter)} – ${monday.plusDays(6).format(rangeFormatter)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterSheetSelector(
    weeks: List<WeekSchedule>,
    selectedWeek: Int,
    currentRealWeek: Int,
    startDate: LocalDate?,
    onWeekSelected: (Int) -> Unit,
    onAddWeek: () -> Unit,
    onDeleteWeek: (Int) -> Unit,
    onRenameWeek: (Int, String) -> Unit,
    onPickStartDate: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var weekToDelete by remember { mutableStateOf<WeekSchedule?>(null) }
    var weekToRename by remember { mutableStateOf<WeekSchedule?>(null) }

    val sortedWeeks = weeks.sortedBy { it.weekNumber }
    val selected = sortedWeeks.find { it.weekNumber == selectedWeek }
    val selectedName = selected?.displayName() ?: "Неделя $selectedWeek"
    val selectedRange = weekRange(startDate, selectedWeek)
    val selectedIsCurrent = selectedWeek == currentRealWeek

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Surface(
            onClick = { showBottomSheet = true },
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (selectedIsCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CurrentBadge()
                        }
                    }
                    if (selectedRange != null) {
                        Text(
                            text = selectedRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
        }

        if (startDate == null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = onPickStartDate,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Укажите дату начала семестра, и недели будут переключаться сами",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    text = "Выберите неделю",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(sortedWeeks, key = { it.weekNumber }) { week ->
                        val isSelected = week.weekNumber == selectedWeek
                        val isReal = week.weekNumber == currentRealWeek
                        val range = weekRange(startDate, week.weekNumber)

                        val borderColor =
                            if (isReal && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val borderWidth = if (isReal) 2.dp else 0.dp

                        ListItem(
                            headlineContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(week.displayName(), maxLines = 1)
                                    if (isReal) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        CurrentBadge()
                                    }
                                }
                            },
                            supportingContent = if (range != null) {
                                { Text(range) }
                            } else null,
                            leadingContent = { RadioButton(selected = isSelected, onClick = null) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { weekToRename = week }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Переименовать",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (sortedWeeks.size > 1) {
                                        IconButton(onClick = { weekToDelete = week }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Удалить",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    onWeekSelected(week.weekNumber)
                                    showBottomSheet = false
                                },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ListItem(
                            headlineContent = { Text("Добавить неделю") },
                            leadingContent = { Icon(Icons.Default.Add, null) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAddWeek()
                                    showBottomSheet = false
                                }
                        )
                        ListItem(
                            headlineContent = { Text("Начало семестра") },
                            supportingContent = {
                                Text(
                                    startDate?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru")))
                                        ?: "Не указано. Недели переключаются вручную"
                                )
                            },
                            leadingContent = { Icon(Icons.Default.DateRange, null) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPickStartDate() }
                        )
                    }
                }
            }
        }
    }

    // Подтверждение удаления
    weekToDelete?.let { week ->
        AlertDialog(
            onDismissRequest = { weekToDelete = null },
            title = { Text("Удалить неделю?") },
            text = { Text("Все занятия недели «${week.displayName()}» будут удалены безвозвратно.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWeek(week.weekNumber)
                        weekToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { weekToDelete = null }) { Text("Отмена") } }
        )
    }

    // Переименование
    weekToRename?.let { week ->
        var text by remember(week.weekNumber) { mutableStateOf(week.name) }
        AlertDialog(
            onDismissRequest = { weekToRename = null },
            title = { Text("Название недели") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(30) },
                    singleLine = true,
                    placeholder = { Text("Неделя ${week.weekNumber}") },
                    supportingText = { Text("Пусто: вернуть «Неделя ${week.weekNumber}»") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onRenameWeek(week.weekNumber, text)
                    weekToRename = null
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { weekToRename = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun CurrentBadge() {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "сейчас",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
