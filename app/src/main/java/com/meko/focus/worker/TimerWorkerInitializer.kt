package com.meko.focus.worker

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager

class TimerWorkerInitializer : Initializer<WorkManager> {
    override fun create(context: Context): WorkManager {
        // 1. 先配置并初始化 WorkManager
        WorkManager.initialize(
            context,
            Configuration.Builder().build()
        )

        // 2. 初始化完成后，通过 getInstance 获取实例并返回
        // 这样就满足了 Initializer<WorkManager> 的返回值要求
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}