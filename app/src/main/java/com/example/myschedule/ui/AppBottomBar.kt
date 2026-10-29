package com.example.myschedule.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

private class BottomTab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    BottomTab("Главная", Icons.Default.Home),
    BottomTab("Расписание", Icons.AutoMirrored.Filled.List),
    BottomTab("Настройки", Icons.Default.Settings)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppBottomBar(
    pagerState: PagerState,
    onTabClick: (Int) -> Unit
) {
    val position by remember {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, (tabs.size - 1).toFloat())
        }
    }

    val selectedIcon = MaterialTheme.colorScheme.onPrimary
    val selectedText = MaterialTheme.colorScheme.primary
    val unselected = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(
            modifier = Modifier
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val itemWidth = maxWidth / tabs.size
            val pillWidth = 64.dp
            val pillHeight = 32.dp
            val pillTop = 12.dp

            Box(
                modifier = Modifier
                    .offset {
                        val item = itemWidth.toPx()
                        IntOffset(
                            x = (item * position + (item - pillWidth.toPx()) / 2f).roundToInt(),
                            y = pillTop.roundToPx()
                        )
                    }
                    .size(pillWidth, pillHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Row(modifier = Modifier.fillMaxSize()) {
                tabs.forEachIndexed { index, tab ->
                    val closeness = (1f - abs(position - index)).coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .selectable(
                                selected = abs(position - index) < 0.5f,
                                onClick = { onTabClick(index) },
                                role = Role.Tab,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(pillTop))
                        Box(
                            modifier = Modifier.size(pillWidth, pillHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = lerp(unselected, selectedIcon, closeness)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = lerp(unselected, selectedText, closeness)
                        )
                    }
                }
            }
        }
    }
}
