package com.meko.focus.worker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.meko.focus.R
import com.meko.focus.util.NotificationHelper
import kotlinx.coroutines.delay

class TimerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 从输入数据中获取计时器状态
        val remainingTimeMs = inputData.getLong(KEY_REMAINING_TIME_MS, 0)
        val sessionType = inputData.getString(KEY_SESSION_TYPE) ?: "FOCUS"

        // 设置前台服务通知
        val notificationId = 1
        val foregroundInfo = createForegroundInfo(notificationId, sessionType, remainingTimeMs)
        setForeground(foregroundInfo)

        // 简单的倒计时循环
        var currentTime = remainingTimeMs
        while (currentTime > 0 && !isStopped) {
            delay(1000)
            currentTime -= 1000

            // 更新通知
            updateNotification(notificationId, sessionType, currentTime)

            // 保存进度
            setProgress(workDataOf(KEY_REMAINING_TIME_MS to currentTime))
        }

        // 计时结束
        if (currentTime <= 0) {
            // 发送完成通知
            sendCompletionNotification(sessionType)
            return Result.success()
        }

        return Result.failure()
    }

    private fun createForegroundInfo(
        notificationId: Int,
        sessionType: String,
        remainingTimeMs: Long
    ): ForegroundInfo {
        val notification = NotificationHelper.createTimerNotificationWithActions(
            applicationContext,
            sessionType,
            remainingTimeMs,
            isTimerRunning = true
        ).build()

        return ForegroundInfo(notificationId, notification)
    }

    private fun updateNotification(
        notificationId: Int,
        sessionType: String,
        remainingTimeMs: Long
    ) {
        val notification = NotificationHelper.createTimerNotificationWithActions(
            applicationContext,
            sessionType,
            remainingTimeMs,
            isTimerRunning = true
        ).build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun sendCompletionNotification(sessionType: String) {
        // 发送计时完成通知
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    companion object {
        const val KEY_REMAINING_TIME_MS = "remaining_time_ms"
        const val KEY_SESSION_TYPE = "session_type"
        const val TIMER_CHANNEL_ID = "timer_channel"
    }
}