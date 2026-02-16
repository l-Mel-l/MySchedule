package com.example.myschedule

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ScheduleRepository(private val context: Context) {


    private val fileName = "my_schedule.json"


    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun saveSchedule(schedule: MainSchedule) {
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

    fun getScheduleFile(): File {
        return File(context.filesDir, fileName)
    }

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