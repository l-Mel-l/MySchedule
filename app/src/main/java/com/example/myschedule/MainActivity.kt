package com.example.myschedule

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myschedule.ui.HomeScreen
import com.example.myschedule.ui.ScheduleScreen
import com.example.myschedule.ui.SettingsScreen
import com.example.myschedule.ui.theme.MyScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyScheduleTheme { MainApp() }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    fun shareSchedule() {
        val context = this
        val repository = ScheduleRepository(context)
        val file = repository.getScheduleFile()

        if (file.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Поделиться расписанием")
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: ScheduleViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // --- ПЕРЕМЕННЫЕ ДЛЯ ИМПОРТА ---
    val activity = context as? MainActivity
    val intent = activity?.intent
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    // Ловим интент (открытие файла)
    LaunchedEffect(intent) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            pendingImportUri = intent.data
            showImportConfirmDialog = true
            activity.intent = null
        }
    }

    // --- ДИАЛОГ 1: ПРИВЕТСТВИЕ ---
    val showWelcomeDialog = !uiState.isLoading &&
            uiState.schedule?.settings?.isFirstLaunch == true &&
            !showImportConfirmDialog

    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.Info, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Привет!", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column {
                    Text("Добро пожаловать в приложение «Моё Расписание».")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Это пет-проект одного студента. Здесь возможны небольшие баги.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Спасибо за использование! \uD83D\uDE0A")
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.disableFirstLaunch() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Всё понятно!") }
            }
        )
    }

    // --- ДИАЛОГ 2: ПОДТВЕРЖДЕНИЕ ИМПОРТА ---
    if (showImportConfirmDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false; pendingImportUri = null },
            title = { Text("Импорт расписания") },
            text = {
                Column {
                    Text("Вы открыли файл расписания.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Хотите заменить текущее расписание на новое? Старые данные будут удалены.", color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportUri?.let { uri ->
                            viewModel.handleImport(
                                uri = uri,
                                onSuccess = { android.widget.Toast.makeText(context, "Обновлено!", android.widget.Toast.LENGTH_SHORT).show() },
                                onError = { android.widget.Toast.makeText(context, "Ошибка файла", android.widget.Toast.LENGTH_SHORT).show() }
                            )
                        }
                        showImportConfirmDialog = false
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Заменить") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false; pendingImportUri = null }) { Text("Отмена") }
            }
        )
    }

    // --- ОСНОВНОЙ UI ---
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Главная") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Расписание") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Настройки") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> ScheduleScreen(viewModel = viewModel)
                2 -> SettingsScreen(
                    viewModel = viewModel,
                    onShareClick = { (context as? MainActivity)?.shareSchedule() }
                )
            }
        }
    }
}