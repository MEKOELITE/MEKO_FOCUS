package com.meko.focus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.meko.focus.MainActivity
import com.meko.focus.R
import com.meko.focus.receiver.NotificationActionReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class TimerForegroundService : Service() {

    private val binder = TimerBinder()
    private var wakeLock: PowerManager.WakeLock? = null
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _remainingTimeMs = MutableStateFlow(0L)
    val remainingTimeMs: StateFlow<Long> = _remainingTimeMs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var sessionType = "FOCUS"
    private var onTimerFinished: (() -> Unit)? = null

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val timeMs = intent.getLongExtra(EXTRA_TIME_MS, 0)
                val type = intent.getStringExtra(EXTRA_SESSION_TYPE) ?: "FOCUS"
                startTimer(timeMs, type)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer(timeMs: Long, type: String) {
        sessionType = type
        _remainingTimeMs.value = timeMs
        _isRunning.value = true

        // 获取WakeLock保持CPU运行
        acquireWakeLock()

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())

        // 开始倒计时
        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_remainingTimeMs.value > 0 && _isRunning.value) {
                delay(1000)
                _remainingTimeMs.value -= 1000
                updateNotification()
            }

            if (_remainingTimeMs.value <= 0) {
                onTimerFinished?.invoke()
                sendCompletionNotification()
                _isRunning.value = false
            }
        }
    }

    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        updateNotification()
    }

    private fun resumeTimer() {
        _isRunning.value = true
        startCountdown()
        updateNotification()
    }

    private fun stopTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        _remainingTimeMs.value = 0
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun setOnTimerFinishedListener(listener: () -> Unit) {
        onTimerFinished = listener
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MEKO_FOCUS:TimerWakeLock"
            ).apply {
                acquire(60 * 60 * 1000L) // 最多1小时
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "计时器",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "番茄钟计时器通知"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (_isRunning.value) {
            NotificationCompat.Action.Builder(
                R.drawable.ic_pause,
                "暂停",
                createActionPendingIntent(ACTION_PAUSE)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                R.drawable.ic_play,
                "继续",
                createActionPendingIntent(ACTION_RESUME)
            ).build()
        }

        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_stop,
            "停止",
            createActionPendingIntent(ACTION_STOP)
        ).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (sessionType == "FOCUS") "专注中" else "休息中")
            .setContentText(formatTime(_remainingTimeMs.value))
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun sendCompletionNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("计时完成")
            .setContentText(if (sessionType == "FOCUS") "专注时间结束！" else "休息结束！")
            .setSmallIcon(R.drawable.ic_timer)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
    }

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        const val COMPLETION_NOTIFICATION_ID = 2

        const val ACTION_START = "com.meko.focus.action.START"
        const val ACTION_PAUSE = "com.meko.focus.action.PAUSE"
        const val ACTION_RESUME = "com.meko.focus.action.RESUME"
        const val ACTION_STOP = "com.meko.focus.action.STOP"

        const val EXTRA_TIME_MS = "extra_time_ms"
        const val EXTRA_SESSION_TYPE = "extra_session_type"

        fun startTimer(context: Context, timeMs: Long, sessionType: String) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TIME_MS, timeMs)
                putExtra(EXTRA_SESSION_TYPE, sessionType)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTimer(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeTimer(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
