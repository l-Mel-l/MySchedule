package com.example.myschedule.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

@Composable
fun MyScheduleTheme(
    content: @Composable () -> Unit
) {
    val WearAppPalette = Colors(
        primary = Color(0xFFFFA500),
        primaryVariant = Color(0xFFE69500),
        secondary = Color(0xFFFFA500),
        background = Color.Black,
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.Black,
        onSurface = Color.White
    )

    MaterialTheme(
        colors = WearAppPalette, // <--- ВОТ ЭТОГО НЕ ХВАТАЛО
        content = content
    )
}