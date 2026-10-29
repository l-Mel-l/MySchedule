package com.example.myschedule.glance

import android.os.SystemClock
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import com.example.myschedule.R

@Composable
fun WidgetTimerText(endMillis: Long, colorArgb: Int, large: Boolean) {
    val context = LocalContext.current
    val layout = if (large) R.layout.widget_chrono_large else R.layout.widget_chrono_small
    val views = RemoteViews(context.packageName, layout)

    val millisLeft = (endMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    views.setChronometerCountDown(R.id.widget_chrono, true)
    views.setChronometer(R.id.widget_chrono, SystemClock.elapsedRealtime() + millisLeft, null, true)
    views.setTextColor(R.id.widget_chrono, colorArgb)

    AndroidRemoteViews(remoteViews = views)
}
