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

    // Добавь это внутрь класса MainActivity
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent // Обновляем текущий интент, чтобы LaunchedEffect в Compose его увидел
    }

    fun shareSchedule() {
        val context = this
        val repository = ScheduleRepository(context)
        val file = repository.getScheduleFile()

        if (file.exists()) {
            try {
                // Получаем безопасную ссылку на файл через FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                // Создаем Интент (намерение) отправить файл
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json" // Тип файла
                    putExtra(Intent.EXTRA_STREAM, uri) // Кладем файл
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Даем временный доступ
                }

                // Запускаем стандартное меню выбора приложения
                val chooser = Intent.createChooser(intent, "Поделиться расписанием")
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
                // Тут можно добавить Toast с ошибкой, если хочешь
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

    // Храним URI файла, который хотят импортировать
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    // Флаг, чтобы показать диалог подтверждения
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    // Ловим интент, но НЕ импортируем сразу, а сохраняем URI и показываем диалог
    LaunchedEffect(intent) {
        if (intent?.action == android.content.Intent.ACTION_VIEW && intent.data != null) {
            pendingImportUri = intent.data
            showImportConfirmDialog = true
            // Очищаем интент, чтобы не срабатывало повторно при повороте
            activity.intent = null
        }
    }

    // --- ДИАЛОГ 1: ПРИВЕТСТВИЕ (О ПРОЕКТЕ) ---
    // Показываем, ТОЛЬКО если не висит диалог импорта (чтобы не накладывались)
    val showWelcomeDialog = !uiState.isLoading &&
            uiState.schedule?.settings?.isFirstLaunch == true &&
            !showImportConfirmDialog // <--- ВАЖНОЕ УСЛОВИЕ

    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Привет!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column {
                    Text("Добро пожаловать в приложение «Моё Расписание».")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Это пет-проект одного студента. Здесь возможны небольшие баги или неточности.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Спасибо за использование! \uD83D\uDE0A") // Смайлик
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.disableFirstLaunch() }, // Выключаем флаг навсегда
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Всё понятно!")
                }
            }
        )
    }

    // --- ДИАЛОГ 2: ПОДТВЕРЖДЕНИЕ ИМПОРТА ---
    if (showImportConfirmDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportUri = null
            },
            title = { Text("Импорт расписания") },
            text = {
                Column {
                    Text("Вы открыли файл расписания.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Хотите заменить текущее расписание на новое? Старые данные будут удалены.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Выполняем импорт
                        pendingImportUri?.let { uri ->
                            viewModel.handleImport(
                                uri = uri,
                                onSuccess = {
                                    android.widget.Toast.makeText(context, "Расписание обновлено!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    android.widget.Toast.makeText(context, "Ошибка файла", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        showImportConfirmDialog = false
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Заменить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportUri = null
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                // Кнопка "Главная"
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
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

                // Кнопка "Расписание"
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
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

                // Кнопка "Настройки"
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
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
            // Получаем текущий контекст (Activity)
            val context = LocalContext.current

            when (selectedTab) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> ScheduleScreen(viewModel = viewModel)
                2 -> SettingsScreen(
                    viewModel = viewModel,
                    onShareClick = {
                        // Теперь это сработает, так как функция внутри MainActivity
                        (context as? MainActivity)?.shareSchedule()
                    }
                )
            }
        }
    }
}