package com.example.myschedule

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class WearScheduleRepository(private val context: Context) {
    private val fileName = "wear_schedule.json"

    // Сохраняем "сырой" JSON, который пришел с телефона
    suspend fun saveJsonString(jsonString: String) {
        withContext(Dispatchers.IO) {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        }
    }

    suspend fun loadSchedule(): MainSchedule? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)
                if (!file.exists()) return@withContext null

                val jsonString = context.openFileInput(fileName).bufferedReader().readText()
                Json { ignoreUnknownKeys = true }.decodeFromString<MainSchedule>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}