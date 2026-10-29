package com.example.myschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == TimerController.ACTION_DISMISSED) {
                    TimerController.onDismissed(appContext)
                } else {
                    TimerController.refresh(appContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
