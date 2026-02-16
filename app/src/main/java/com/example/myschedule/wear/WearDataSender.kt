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
                val jsonString = json.encodeToString(schedule)

                val request = PutDataMapRequest.create("/schedule").apply {
                    dataMap.putString("schedule_json", jsonString)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }

                dataClient.putDataItem(request.asPutDataRequest()).await()

                println("Wear: Schedule sent successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}