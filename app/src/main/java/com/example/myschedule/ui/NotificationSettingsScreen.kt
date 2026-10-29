package com.example.myschedule.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myschedule.notification.NotificationPrefs
import com.example.myschedule.notification.TimerController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { NotificationPrefs(context) }

    var enabled by remember { mutableStateOf(prefs.enabled) }
    var allowSwipe by remember { mutableStateOf(prefs.allowSwipeAway) }
    var showNext by remember { mutableStateOf(prefs.showNextLesson) }
    var showBreaks by remember { mutableStateOf(prefs.showBreaks) }
    var preMinutes by remember { mutableStateOf(prefs.preLessonMinutes) }
    var cardStyle by remember { mutableStateOf(prefs.cardStyle) }
    var permissionDenied by remember { mutableStateOf(false) }

    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    val reallyEnabled = enabled && hasPermission()

    fun refresh() {
        scope.launch { TimerController.refresh(context) }
    }

    fun turnOn() {
        prefs.enabled = true
        enabled = true
        permissionDenied = false
        refresh()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) turnOn() else permissionDenied = true
    }

    fun onMainSwitch(wantOn: Boolean) {
        if (!wantOn) {
            prefs.enabled = false
            enabled = false
            refresh()
            return
        }
        if (!hasPermission()) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            turnOn()
        }
    }

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
            Text("Уведомление", style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            "Показывает, сколько осталось до конца занятия, прямо в шторке и на экране блокировки. " +
                    "Таймер тикает силами системы, батарею почти не тратит.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        SwitchRow(
            title = "Показывать уведомление",
            description = null,
            checked = reallyEnabled,
            onCheckedChange = { onMainSwitch(it) },
            bold = true
        )

        if (permissionDenied) {
            Text(
                "Без разрешения на уведомления таймер показать не получится. " +
                        "Разрешить можно в настройках телефона: Приложения → My Schedule → Уведомления.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (reallyEnabled) {
            SwitchRow(
                title = "Закрепить",
                description = "Нельзя смахнуть: если смахнул, уведомление вернётся.",
                checked = !allowSwipe,
                onCheckedChange = {
                    allowSwipe = !it
                    prefs.allowSwipeAway = !it
                    refresh()
                }
            )
            SwitchRow(
                title = "Показывать перемены",
                description = "Во время перемены считает время до начала следующего занятия.",
                checked = showBreaks,
                onCheckedChange = {
                    showBreaks = it
                    prefs.showBreaks = it
                    refresh()
                }
            )
            SwitchRow(
                title = "Строка «Далее»",
                description = "В развёрнутом уведомлении показывает следующее занятие.",
                checked = showNext,
                onCheckedChange = {
                    showNext = it
                    prefs.showNextLesson = it
                    refresh()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Таймер до первого занятия", fontWeight = FontWeight.Medium)
            Text(
                "За сколько до начала первого занятия дня появится уведомление.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Нет", 15 to "15 мин", 30 to "30 мин", 60 to "1 час").forEach { (minutes, label) ->
                    FilterChip(
                        selected = preMinutes == minutes,
                        onClick = {
                            preMinutes = minutes
                            prefs.preLessonMinutes = minutes
                            refresh()
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Вид уведомления", fontWeight = FontWeight.Medium)
            Text(
                "«Как в системе» берёт тему телефона в момент показа и обновляется при открытии приложения.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    NotificationPrefs.STYLE_AUTO to "Как в системе",
                    NotificationPrefs.STYLE_DARK to "Тёмное",
                    NotificationPrefs.STYLE_LIGHT to "Светлое"
                ).forEach { (style, label) ->
                    FilterChip(
                        selected = cardStyle == style,
                        onClick = {
                            cardStyle = style
                            prefs.cardStyle = style
                            refresh()
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { TimerController.showPreview(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Показать пример уведомления")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Уведомление не переключается вовремя?", fontWeight = FontWeight.Medium)
            Text(
                "Некоторые телефоны ограничивают работу приложений в фоне, из-за этого уведомление может " +
                        "переключаться с опозданием. Откройте настройки приложения и разрешите ему работать " +
                        "в фоне без ограничений (обычно это разделы «Батарея» и «Автозапуск»).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts("package", context.packageName, null)
                        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Открыть настройки приложения")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
            if (description != null) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
