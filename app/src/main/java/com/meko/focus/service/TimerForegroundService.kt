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

/**
 * 计时器前台服务
 *
 * 负责在后台持续运行计时器，确保即使应用进入后台或屏幕关闭时计时仍能正常进行。
 * 使用 WakeLock 防止 CPU 进入休眠，并显示持续性通知展示当前状态。
 */
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

    /**
     * 服务绑定器
     *
     * 允许 Activity/ViewModel 绑定到此服务以获取服务实例的引用，
     * 从而直接访问服务内部的 StateFlow。
     */
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

    /**
     * 启动计时器
     *
     * @param timeMs 倒计时总时长（毫秒）
     * @param type 会话类型（"FOCUS" 或其他）
     */
    private fun startTimer(timeMs: Long, type: String) {
        sessionType = type
        
        // 防御性检查：确保时间有效
        val validTimeMs = if (timeMs <= 0) {
            25 * 60 * 1000L // 默认25分钟
        } else {
            timeMs
        }
        
        _remainingTimeMs.value = validTimeMs
        _isRunning.value = true

        acquireWakeLock()
        startForeground(NOTIFICATION_ID, createNotification())
        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_remainingTimeMs.value > 0 && _isRunning.value) {
                delay(1000)
                val currentTime = _remainingTimeMs.value
                if (currentTime > 0) {
                    _remainingTimeMs.value = currentTime - 1000
                    updateNotification()
                }
            }

            // 使用局部变量避免竞态条件
            val finalTime = _remainingTimeMs.value
            val wasRunning = _isRunning.value
            
            if (finalTime <= 0 && wasRunning) {
                _isRunning.value = false
                onTimerFinished?.invoke()
                sendCompletionNotification()
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

    /**
     * 设置计时器完成回调
     */
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
                acquire(60 * 60 * 1000L)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        /**
         * 启动计时器服务
         */
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
