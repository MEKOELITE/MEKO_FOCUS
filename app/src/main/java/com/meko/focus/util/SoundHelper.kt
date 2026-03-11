package com.meko.focus.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build

object SoundHelper {
    private var soundPool: SoundPool? = null
    private var tickSoundId = 0
    private var completeSoundId = 0
    private var isInitialized = false
    private var isEnabled = true

    fun initialize(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(2, android.media.AudioManager.STREAM_MUSIC, 0)
        }

        // 加载声音资源（需要添加声音文件到res/raw）
        // tickSoundId = soundPool?.load(context, R.raw.tick_sound, 1) ?: 0
        // completeSoundId = soundPool?.load(context, R.raw.complete_sound, 1) ?: 0

        isInitialized = true
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playTick() {
        if (!isEnabled || !isInitialized) return
        soundPool?.play(tickSoundId, 0.1f, 0.1f, 1, 0, 1.0f)
    }

    fun playComplete() {
        if (!isEnabled || !isInitialized) return
        soundPool?.play(completeSoundId, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}