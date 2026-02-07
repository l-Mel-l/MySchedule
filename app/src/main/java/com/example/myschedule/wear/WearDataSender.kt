package com.example.myschedule.wear

import android.content.Context
import com.example.myschedule.MainSchedule
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WearDataSender(private val context: Context) {

    private val dataClient = Wearable.getDataClient(context)
    private val json = Json { ignoreUnknownKeys = true }

    // Функция отправки расписания
    suspend fun sendScheduleToWatch(schedule: MainSchedule) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Превращаем объект в строку JSON
                val jsonString = json.encodeToString(schedule)

                // 2. Создаем запрос на отправку (PutDataRequest)
                // Путь "/schedule" - это как "тема письма", часы будут слушать именно этот путь
                val request = PutDataMapRequest.create("/schedule").apply {
                    dataMap.putString("schedule_json", jsonString)
                    // Добавляем timestamp, чтобы данные всегда считались новыми (иначе часы могут проигнорировать дубликат)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }

                // 3. Отправляем
                dataClient.putDataItem(request.asPutDataRequest()).await()

                // (В логах можно будет увидеть успех, но пока без UI)
                println("Wear: Schedule sent successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}