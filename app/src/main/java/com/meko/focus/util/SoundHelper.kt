package com.meko.focus.util

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SoundHelper {
    private var ringtone: android.media.Ringtone? = null
    private var isEnabled = true

    fun initialize(context: Context) {
        if (ringtone != null) return

        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(context, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playTick() {
        // Not implemented - tick sound not needed
    }

    fun playComplete() {
        if (!isEnabled || ringtone == null) return
        try {
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        ringtone?.stop()
        ringtone = null
    }
}
