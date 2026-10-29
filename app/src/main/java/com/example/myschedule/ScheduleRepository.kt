package com.example.myschedule

import android.content.Context
import android.util.AtomicFile
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
            val atomicFile = AtomicFile(getScheduleFile())
            val output = atomicFile.startWrite()
            try {
                val jsonString = json.encodeToString(schedule)
                output.write(jsonString.toByteArray())
                atomicFile.finishWrite(output)
            } catch (e: Exception) {
                atomicFile.failWrite(output)
                e.printStackTrace()
            }
        }
    }

    suspend fun loadSchedule(): MainSchedule? {
        return withContext(Dispatchers.IO) {
            val file = getScheduleFile()
            val backup = File(file.path + ".bak")
            if (!file.exists() && !backup.exists()) return@withContext null

            try {
                val jsonString = AtomicFile(file).readFully().toString(Charsets.UTF_8)
                json.decodeFromString<MainSchedule>(jsonString).healed()
            } catch (e: Exception) {
                e.printStackTrace()
                keepBrokenCopy(file)
                null
            }
        }
    }

    private fun keepBrokenCopy(file: File) {
        try {
            if (file.exists()) {
                file.copyTo(File(file.parentFile, "my_schedule.broken.json"), overwrite = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                    json.decodeFromString<MainSchedule>(jsonString).healed()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
