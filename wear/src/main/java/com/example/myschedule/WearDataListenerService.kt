package com.example.myschedule

import android.content.Context
import androidx.wear.tiles.TileService // <--- ВАЖНЫЙ ИМПОРТ
import com.example.myschedule.tile.MainTileService // <--- ТВОЯ ПЛИТКА
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearDataListenerService : WearableListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var repository: WearScheduleRepository

    override fun onCreate() {
        super.onCreate()
        repository = WearScheduleRepository(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        println("Wear: Service onDataChanged called!")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/schedule") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val jsonString = dataMap.getString("schedule_json")

                    if (jsonString != null) {
                        scope.launch {
                            // 1. Сохраняем файл
                            repository.saveJsonString(jsonString)
                            println("Wear: Schedule received and saved!")

                            // 2. --- ПИНАЕМ ПЛИТКУ (ОБНОВИСЬ!) ---
                            try {
                                TileService.getUpdater(this@WearDataListenerService)
                                    .requestUpdate(MainTileService::class.java)
                                println("Wear: Tile update requested")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }
}