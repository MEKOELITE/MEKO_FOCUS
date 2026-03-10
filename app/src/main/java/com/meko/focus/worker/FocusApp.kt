package com.meko.focus

import android.app.Application
import com.meko.focus.util.NotificationHelper // 记得导入这个
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 将原先 PomodoroApp 里的通知渠道创建逻辑挪到这里
        NotificationHelper.createTimerNotificationChannel(this)
    }
}