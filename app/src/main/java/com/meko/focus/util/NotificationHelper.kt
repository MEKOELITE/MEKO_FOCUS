package com.meko.focus.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.meko.focus.R

object NotificationHelper {
    const val TIMER_CHANNEL_ID = "timer_channel"
    const val TIMER_NOTIFICATION_ID = 1

    fun createTimerNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val description = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(TIMER_CHANNEL_ID, name, importance).apply {
                this.description = description
                setSound(null, null) // 无声音
                enableVibration(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createTimerNotification(
        context: Context,
        sessionType: String,
        remainingTimeMs: Long
    ): NotificationCompat.Builder {
        val timeText = formatTime(remainingTimeMs)
        val contentText = context.getString(
            R.string.notification_content,
            sessionType,
            timeText
        )

        return NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}