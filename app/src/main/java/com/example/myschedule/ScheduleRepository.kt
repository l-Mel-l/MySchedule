package com.example.myschedule

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ScheduleRepository(private val context: Context) {

    // Имя файла, где будет лежать расписание
    private val fileName = "my_schedule.json"

    // Настройка JSON (prettyPrint = true делает файл читаемым для человека, если ты его откроешь блокнотом)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true // Чтобы приложение не падало, если формат файла чуть изменится в будущем
    }

    // Функция сохранения (Запись в файл)
    suspend fun saveSchedule(schedule: MainSchedule) {
        // Переключаемся на фоновый поток (IO), чтобы не тормозить интерфейс
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(schedule)
                context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                    output.write(jsonString.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Функция загрузки (Чтение из файла)
    suspend fun loadSchedule(): MainSchedule? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)
                if (!file.exists()) return@withContext null

                val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
                json.decodeFromString<MainSchedule>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Функция для экспорта файла (чтобы поделиться)
    fun getScheduleFile(): File {
        return File(context.filesDir, fileName)
    }

    // Просто читаем и возвращаем объект, НЕ сохраняя в файл
    suspend fun readScheduleFromUri(uri: android.net.Uri): MainSchedule? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().readText()
                    json.decodeFromString<MainSchedule>(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}