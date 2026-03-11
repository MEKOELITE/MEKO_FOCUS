package com.meko.focus.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.meko.focus.R
import com.meko.focus.receiver.NotificationActionReceiver

object NotificationHelper {
    // --- 常量定义（直接放在 object 根部，方便外部引用） ---
    const val TIMER_CHANNEL_ID = "timer_channel"
    const val TIMER_NOTIFICATION_ID = 1

    const val ACTION_PAUSE = "com.meko.focus.ACTION_PAUSE"
    const val ACTION_RESUME = "com.meko.focus.ACTION_RESUME"
    const val ACTION_SKIP = "com.meko.focus.ACTION_SKIP"
    const val ACTION_STOP = "com.meko.focus.ACTION_STOP"

    const val EXTRA_SESSION_TYPE = "session_type"
    const val EXTRA_REMAINING_TIME_MS = "remaining_time_ms"

    /**
     * 创建通知渠道（Android 8.0+ 必须调用）
     */
    fun createTimerNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val description = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(TIMER_CHANNEL_ID, name, importance).apply {
                this.description = description
                setSound(null, null)
                enableVibration(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 基础通知创建（用于 Service 启动时）
     */
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

    /**
     * 带交互按钮的通知（用于实时更新状态）
     */
    fun createTimerNotificationWithActions(
        context: Context,
        sessionType: String,
        remainingTimeMs: Long,
        isTimerRunning: Boolean = true
    ): NotificationCompat.Builder {
        val timeText = formatTime(remainingTimeMs)
        val contentText = context.getString(
            R.string.notification_content,
            sessionType,
            timeText
        )

        // 计算进度
        val totalDurationMs = getTotalDurationForSession(sessionType)
        val progress = if (totalDurationMs > 0) {
            ((totalDurationMs - remainingTimeMs) * 100 / totalDurationMs).toInt()
        } else {
            0
        }

        // 创建 PendingIntent 辅助函数
        fun createPendingIntent(action: String): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                this.action = action
                putExtra(EXTRA_SESSION_TYPE, sessionType)
                putExtra(EXTRA_REMAINING_TIME_MS, remainingTimeMs)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
        }

        val builder = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setProgress(100, progress, false)

        // 添加“开始/暂停”按钮
        if (isTimerRunning) {
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.notification_action_pause),
                createPendingIntent(ACTION_PAUSE)
            )
        } else {
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.notification_action_resume),
                createPendingIntent(ACTION_RESUME)
            )
        }

        // 添加“跳过”按钮
        builder.addAction(
            R.drawable.ic_launcher_foreground,
            context.getString(R.string.notification_action_skip),
            createPendingIntent(ACTION_SKIP)
        )

        return builder
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun getTotalDurationForSession(sessionType: String): Long {
        return when (sessionType) {
            "FOCUS" -> 25L * 60 * 1000
            "SHORT_BREAK" -> 5L * 60 * 1000
            "LONG_BREAK" -> 15L * 60 * 1000
            "BREAK" -> 5L * 60 * 1000
            else -> 25L * 60 * 1000
        }
    }
}