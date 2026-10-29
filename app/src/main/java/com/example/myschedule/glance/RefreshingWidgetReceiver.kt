package com.example.myschedule.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.myschedule.notification.TimerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class RefreshingWidgetReceiver : GlanceAppWidgetReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val interesting = setOf(
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            "android.appwidget.action.APPWIDGET_ENABLED",
            "android.appwidget.action.APPWIDGET_DISABLED",
            "android.appwidget.action.APPWIDGET_DELETED"
        )
        if (intent.action !in interesting) return

        val pending = try {
            goAsync()
        } catch (e: Exception) {
            null
        }
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TimerController.refresh(app)
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                try {
                    pending?.finish()
                } catch (e: Exception) {
                    // уже завершён
                }
            }
        }
    }
}
