package com.example.myschedule

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.myschedule.ui.AppBottomBar
import com.example.myschedule.ui.HomeScreen
import com.example.myschedule.ui.NotificationSettingsScreen
import com.example.myschedule.ui.ScheduleScreen
import com.example.myschedule.ui.SettingsScreen
import com.example.myschedule.ui.theme.MyScheduleTheme

class MainActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            com.example.myschedule.notification.TimerController.refresh(applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.myschedule.ui.theme.Appearance.load(this)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainApp(viewModel: ScheduleViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var showNotificationSettings by rememberSaveable { mutableStateOf(false) }
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showNotificationSettings || showAppearance) {
        showNotificationSettings = false
        showAppearance = false
    }

    var preloadPages by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(600)
        preloadPages = 2
    }

    fun goToTab(page: Int) {
        scope.launch {
            if (showNotificationSettings || showAppearance) {
                pagerState.scrollToPage(page)
                showNotificationSettings = false
                showAppearance = false
            } else {
                pagerState.animateScrollToPage(
                    page = page,
                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                )
            }
        }
    }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val activity = context as? MainActivity
    val intent = activity?.intent
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var triggerImport by remember { mutableStateOf(false) }

    LaunchedEffect(intent) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            pendingImportUri = intent.data
            showImportConfirmDialog = true
            activity.intent = null
        }
        when (intent?.action) {
            "com.example.myschedule.ACTION_SHARE" -> {
                activity.shareSchedule()
                activity.intent = null
            }
            "com.example.myschedule.ACTION_IMPORT" -> {
                showNotificationSettings = false
                showAppearance = false
                pagerState.scrollToPage(2)
                triggerImport = true
                activity.intent = null
            }
            "com.example.myschedule.ACTION_SYNC_WATCH" -> {
                if (uiState.schedule != null) {
                    viewModel.syncToWatch()
                    android.widget.Toast.makeText(
                        context,
                        "Данные отправлены на часы",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Сначала создайте или импортируйте расписание",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                activity.intent = null
            }
        }
    }

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

    Scaffold(
        bottomBar = {
            AppBottomBar(pagerState = pagerState, onTabClick = { goToTab(it) })
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondBoundsPageCount = preloadPages
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(viewModel = viewModel)
                        1 -> ScheduleScreen(viewModel = viewModel)
                        else -> SettingsScreen(
                            viewModel = viewModel,
                            onShareClick = { (context as? MainActivity)?.shareSchedule() },
                            onSyncWatchClick = { viewModel.syncToWatch() },
                            onOpenNotificationSettings = { showNotificationSettings = true },
                            onOpenAppearance = { showAppearance = true },
                            triggerImport = triggerImport,
                            onImportTriggered = { triggerImport = false }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showNotificationSettings,
                    enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it },
                    exit = slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NotificationSettingsScreen(onBack = { showNotificationSettings = false })
                    }
                }

                // Экран «Внешний вид»
                AnimatedVisibility(
                    visible = showAppearance,
                    enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it },
                    exit = slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        com.example.myschedule.ui.AppearanceScreen(onBack = { showAppearance = false })
                    }
                }
            }
        }
    }
}
