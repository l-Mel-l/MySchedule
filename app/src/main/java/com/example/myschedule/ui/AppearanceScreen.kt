package com.example.myschedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.example.myschedule.ui.theme.Appearance
import com.example.myschedule.ui.theme.StyleColors
import com.example.myschedule.ui.theme.styleColors

private val accentPalette = listOf(
    0xFFFFA500, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF3949AB,
    0xFF1E88E5, 0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFF90A4AE
).map { it.toInt() }

private val seedPalette = listOf(
    0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF7CB342, 0xFF00897B, 0xFF00ACC1,
    0xFF1E88E5, 0xFF3949AB, 0xFF8E24AA, 0xFFD81B60, 0xFF6D4C41, 0xFF78909C
).map { it.toInt() }

private val cardPaletteDark = listOf(
    0xFF1E1E1E, 0xFF2B2B33, 0xFF3D3D45, 0xFF1F3347, 0xFF34445E,
    0xFF1F3A36, 0xFF22382A, 0xFF3F3558, 0xFF3A2430, 0xFF3A2A22, 0xFF3B3320
).map { it.toInt() }

private val cardPaletteLight = listOf(
    0xFFFFFFFF, 0xFFF1F1F4, 0xFFE4E6EB, 0xFFD9DEE7, 0xFFE3F2FD,
    0xFFE0F2F1, 0xFFE8F5E9, 0xFFFFF8E1, 0xFFFFF3E0, 0xFFFCE4EC, 0xFFEDE7F6
).map { it.toInt() }

@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val pure = Appearance.pureBlack && isDark
    val autoCard = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    val styleNow = styleColors(Appearance.styleId, Appearance.seedColor(), isDark, Appearance.tintBackground, pure)
    var advancedOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("Внешний вид", style = MaterialTheme.typography.headlineMedium)
        }

        Preview(autoCard)

        SectionTitle("Режим")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Appearance.THEME_SYSTEM to "Как в системе",
                Appearance.THEME_LIGHT to "Светлая",
                Appearance.THEME_DARK to "Тёмная"
            ).forEach { (mode, label) ->
                FilterChip(
                    selected = Appearance.themeMode == mode,
                    onClick = { Appearance.setThemeMode(context, mode) },
                    label = { Text(label) }
                )
            }
        }

        SectionTitle("Стиль", "Красит фон, карточки и акцент сразу. У каждого стиля есть светлая и тёмная версия")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            Appearance.styles.forEach { def ->
                StyleTile(
                    name = def.name,
                    colors = styleColors(def.id, def.seed, isDark, Appearance.tintBackground, pure),
                    selected = Appearance.styleId == def.id,
                    onClick = { Appearance.setStyle(context, def.id) }
                )
            }
            StyleTile(
                name = "Свой",
                colors = styleColors(
                    Appearance.STYLE_CUSTOM, Appearance.customSeed, isDark, Appearance.tintBackground, pure
                ),
                selected = Appearance.styleId == Appearance.STYLE_CUSTOM,
                onClick = { Appearance.setStyle(context, Appearance.STYLE_CUSTOM) }
            )
        }

        if (Appearance.styleId == Appearance.STYLE_CUSTOM) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Главный цвет", fontWeight = FontWeight.Medium)
            Text(
                "Выберите один цвет: фон, карточки и акцент подберутся под него сами.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            seedPalette.chunked(6).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    rowItems.forEach { value ->
                        Swatch(
                            color = Color(value),
                            isDefault = false,
                            isSelected = Appearance.customSeed == value,
                            onClick = { Appearance.setCustomSeed(context, value) }
                        )
                    }
                }
            }
            val hsl = FloatArray(3).also { ColorUtils.colorToHSL(Appearance.customSeed, it) }
            Text("Оттенок", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = hsl[0],
                onValueChange = {
                    val argb = ColorUtils.HSLToColor(floatArrayOf(it, 0.65f, 0.5f))
                    Appearance.setCustomSeed(context, argb, save = false)
                },
                onValueChangeFinished = { Appearance.saveCustomSeed(context) },
                valueRange = 0f..360f
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (Appearance.styleId != Appearance.STYLE_CLASSIC) {
            SwitchRow(
                title = "Оттенок фона",
                description = "Фон и карточки в цвет стиля. Выключите, чтобы они остались серыми.",
                checked = Appearance.tintBackground,
                onCheckedChange = { Appearance.setTintBackground(context, it) }
            )
        }
        if (isDark) {
            SwitchRow(
                title = "Чисто чёрный фон",
                description = "Для тёмной темы. Экономит заряд на экранах AMOLED.",
                checked = Appearance.pureBlack,
                onCheckedChange = { Appearance.setPureBlack(context, it) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { advancedOpen = !advancedOpen }
                .padding(vertical = 14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Дополнительно", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Отдельные цвета поверх стиля",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                if (advancedOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        if (advancedOpen) {
            SectionTitle("Акцентный цвет", "Кнопки, метки и выделения. «Авто» = как в стиле")
            ColorRow(
                palette = accentPalette,
                selected = Appearance.accent,
                defaultColor = styleNow.accent,
                onPick = { Appearance.setAccent(context, it) }
            )

            SectionTitle("Карточки занятий", "Цвет фона карточек в расписании")
            ColorRow(
                palette = if (isDark) cardPaletteDark else cardPaletteLight,
                selected = Appearance.lessonCard,
                defaultColor = autoCard,
                onPick = { Appearance.setLessonCard(context, it) }
            )

            SectionTitle("Круг с таймером", "«Авто» = как акцентный цвет")
            ColorRow(
                palette = accentPalette,
                selected = Appearance.timerRing,
                defaultColor = MaterialTheme.colorScheme.primary,
                onPick = { Appearance.setTimerRing(context, it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = { Appearance.resetAll(context) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Сбросить внешний вид") }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StyleTile(name: String, colors: StyleColors, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 84.dp, height = 96.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.background)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surfaceVariant)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.accent)
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.align(Alignment.BottomEnd).size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun Preview(autoCard: Color) {
    val ring = if (Appearance.timerRing != 0) Color(Appearance.timerRing) else MaterialTheme.colorScheme.primary
    val cardBg = if (Appearance.lessonCard != 0) Color(Appearance.lessonCard) else autoCard
    val cardText = if (Appearance.lessonCard != 0) Appearance.onColorFor(cardBg) else MaterialTheme.colorScheme.onSurface

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(84.dp),
                    color = MaterialTheme.colorScheme.background,
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier.size(84.dp),
                    color = ring,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Text("1:13", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg, contentColor = cardText),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Математика", fontWeight = FontWeight.Bold)
                    Text("10:00 – 11:30 · 305", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {}) { Text("Кнопка") }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, hint: String? = null) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium)
    if (hint != null) {
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorRow(
    palette: List<Int>,
    selected: Int,
    defaultColor: Color,
    onPick: (Int) -> Unit
) {
    val items = listOf(0) + palette
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(6).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { value ->
                    val color = if (value == 0) defaultColor else Color(value)
                    Swatch(
                        color = color,
                        isDefault = value == 0,
                        isSelected = selected == value,
                        onClick = { onPick(value) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Swatch(color: Color, isDefault: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else outline.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Appearance.onColorFor(color))
        } else if (isDefault) {
            Text("авто", style = MaterialTheme.typography.labelSmall, color = Appearance.onColorFor(color))
        }
    }
}
