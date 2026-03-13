package com.meko.focus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.util.Log
import com.meko.focus.service.TimerForegroundService
import com.meko.focus.util.NotificationHelper

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        Log.d(TAG, "Received action: $action")

        when (action) {
            NotificationHelper.ACTION_PAUSE -> {
                TimerForegroundService.pauseTimer(context)
                Log.d(TAG, "Timer paused")
            }
            NotificationHelper.ACTION_RESUME -> {
                TimerForegroundService.resumeTimer(context)
                Log.d(TAG, "Timer resumed")
            }
            NotificationHelper.ACTION_SKIP -> {
                // Skip is handled by the service after notifying
                Log.d(TAG, "Timer skip requested")
            }
            NotificationHelper.ACTION_STOP -> {
                TimerForegroundService.stopTimer(context)
                Log.d(TAG, "Timer stopped")
            }
        }

        // Cancel the notification (except for pause which updates the notification)
        if (action != NotificationHelper.ACTION_PAUSE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NotificationHelper.TIMER_NOTIFICATION_ID)
        }
    }
}
