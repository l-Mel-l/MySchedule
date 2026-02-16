package com.example.myschedule.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterSheetSelector(
    selectedWeek: Int,
    currentRealWeek: Int,
    existingWeeks: List<Int>,
    onWeekSelected: (Int) -> Unit,
    onAddWeek: () -> Unit,
    onDeleteWeek: (Int) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var weekToDelete by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = { showBottomSheet = true },
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Неделя $selectedWeek",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Выберите неделю",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    val sortedWeeks = existingWeeks.sorted().toMutableList()
                    if (!sortedWeeks.contains(selectedWeek)) {
                        sortedWeeks.add(selectedWeek)
                        sortedWeeks.sort()
                    }

                    items(sortedWeeks) { weekNum ->
                        val isSelected = weekNum == selectedWeek
                        val isReal = weekNum == currentRealWeek

                        val borderColor = if (isReal && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val borderWidth = if (isReal) 2.dp else 0.dp

                        ListItem(
                            headlineContent = {
                                Row {
                                    Text("Неделя $weekNum")
                                    if (isReal) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("(сейчас)", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            },
                            leadingContent = {
                                RadioButton(selected = isSelected, onClick = null)
                            },
                            trailingContent = {
                                if (existingWeeks.contains(weekNum)) {
                                    IconButton(onClick = { weekToDelete = weekNum }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Удалить",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                if (weekToDelete != null) {
                                    AlertDialog(
                                        onDismissRequest = { weekToDelete = null },
                                        title = { Text("Удалить неделю?") },
                                        text = { Text("Все занятия Недели ${weekToDelete} будут удалены безвозвратно.") },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    weekToDelete?.let { onDeleteWeek(it) }
                                                    weekToDelete = null
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = androidx.compose.ui.graphics.Color(0xFFD32F2F),
                                                    contentColor = androidx.compose.ui.graphics.Color.White
                                                )
                                            ) {
                                                Text("Удалить")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { weekToDelete = null }) {
                                                Text("Отмена")
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    onWeekSelected(weekNum)
                                    showBottomSheet = false
                                },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ListItem(
                            headlineContent = { Text("Добавить следующую неделю") },
                            leadingContent = { Icon(Icons.Default.Add, null) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAddWeek()
                                    showBottomSheet = false
                                }
                        )
                    }
                }
            }
        }
    }
}