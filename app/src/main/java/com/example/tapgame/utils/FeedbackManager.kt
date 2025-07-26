package com.example.tapgame.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import com.example.tapgame.data.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class FeedbackManager(private val context: Context, private val settingsDataStore: SettingsDataStore) {

    private val vibrator = context.getSystemService<Vibrator>()
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)

    fun playClickSound() {
        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 100)
    }

    fun vibrate() {
        vibrator?.vibrate(
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    suspend fun performFeedback() {
        withContext(Dispatchers.Main) {
           // val vibrateEnabled = settingsDataStore.vibrationEnabledFlow.first()
           // val soundEnabled = settingsDataStore.soundEnabledFlow.first()
            /*if (vibrateEnabled) {
                vibrate()
            }
            if (soundEnabled) {
                playClickSound()
            }*/
        }
    }
}