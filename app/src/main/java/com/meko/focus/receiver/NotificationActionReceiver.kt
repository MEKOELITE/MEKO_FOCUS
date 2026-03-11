package com.meko.focus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager // 显式导入
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.meko.focus.util.NotificationHelper
import com.meko.focus.worker.TimerWorker
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        // 修复点：使用 NotificationHelper 前缀来引用常量
        val sessionType = intent.getStringExtra(NotificationHelper.EXTRA_SESSION_TYPE) ?: "FOCUS"
        val remainingTimeMs = intent.getLongExtra(NotificationHelper.EXTRA_REMAINING_TIME_MS, 0L)

        Log.d(TAG, "Received action: $action, sessionType: $sessionType, remainingTimeMs: $remainingTimeMs")

        when (action) {
            NotificationHelper.ACTION_PAUSE -> handlePauseAction(context, sessionType, remainingTimeMs)
            NotificationHelper.ACTION_RESUME -> handleResumeAction(context, sessionType, remainingTimeMs)
            NotificationHelper.ACTION_SKIP -> handleSkipAction(context, sessionType)
            NotificationHelper.ACTION_STOP -> handleStopAction(context)
        }

        // 这里的清理逻辑也可以正常执行
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 注意：如果是暂停操作，我们在 handlePauseAction 里重新发了新通知，所以这里不要 cancel 掉
        if (action != NotificationHelper.ACTION_PAUSE) {
            notificationManager.cancel(NotificationHelper.TIMER_NOTIFICATION_ID)
        }
    }

    private fun handlePauseAction(context: Context, sessionType: String, remainingTimeMs: Long) {
        cancelTimerWorker(context)
        Log.d(TAG, "Timer paused: $sessionType, remaining: $remainingTimeMs ms")
        showPausedNotification(context, sessionType, remainingTimeMs)
    }

    private fun handleResumeAction(context: Context, sessionType: String, remainingTimeMs: Long) {
        startTimerWorker(context, sessionType, remainingTimeMs)
        Log.d(TAG, "Timer resumed: $sessionType, remaining: $remainingTimeMs ms")
    }

    private fun handleSkipAction(context: Context, sessionType: String) {
        Log.d(TAG, "Timer skipped: $sessionType")
        val nextSessionType = when (sessionType) {
            "FOCUS" -> "BREAK"
            "BREAK" -> "FOCUS"
            else -> "FOCUS"
        }
        val nextDuration = when (nextSessionType) {
            "FOCUS" -> 25L * 60 * 1000
            "BREAK" -> 5L * 60 * 1000
            else -> 25L * 60 * 1000
        }
        startTimerWorker(context, nextSessionType, nextDuration)
    }

    private fun handleStopAction(context: Context) {
        cancelTimerWorker(context)
        Log.d(TAG, "Timer stopped")
    }

    private fun startTimerWorker(context: Context, sessionType: String, remainingTimeMs: Long) {
        val inputData = Data.Builder()
            .putLong(TimerWorker.KEY_REMAINING_TIME_MS, remainingTimeMs)
            .putString(TimerWorker.KEY_SESSION_TYPE, sessionType)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TimerWorker>()
            .setInputData(inputData)
            .addTag("timer_worker")
            .setInitialDelay(0, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun cancelTimerWorker(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("timer_worker")
    }

    private fun showPausedNotification(context: Context, sessionType: String, remainingTimeMs: Long) {
        // 修复点：使用标准的 getSystemService 获取管理器
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationHelper.createTimerNotificationWithActions(
            context,
            sessionType,
            remainingTimeMs,
            isTimerRunning = false
        )

        notificationManager.notify(NotificationHelper.TIMER_NOTIFICATION_ID, notificationBuilder.build())
    }
}