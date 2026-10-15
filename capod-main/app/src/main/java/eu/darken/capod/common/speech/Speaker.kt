package eu.darken.capod.common.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.capod.R
import eu.darken.capod.common.debug.logging.Logging.Priority.WARN
import eu.darken.capod.common.debug.logging.log
import eu.darken.capod.common.debug.logging.logTag
import eu.darken.capod.monitor.core.PodDevice
import eu.darken.capod.pods.core.apple.ble.BATTERY_UNKNOWN
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Andropods addition: on-demand TextToSpeech for battery announcements
 * and the [eu.darken.capod.reaction.core.stem.StemAction.AnnounceBattery] gesture.
 * Engine is created lazily and kept; failures are swallowed (best-effort).
 */
@Singleton
class Speaker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mutex = Mutex()
    private var engine: TextToSpeech? = null

    private suspend fun engine(): TextToSpeech? = mutex.withLock {
        engine?.let { return it }
        val created = suspendCoroutine<TextToSpeech?> { cont ->
            var resumed = false
            try {
                val tts = TextToSpeech(context) { status ->
                    if (resumed) return@TextToSpeech
                    resumed = true
                    if (status == TextToSpeech.SUCCESS) cont.resume(tts) else {
                        runCatching { tts.shutdown() }
                        cont.resume(null)
                    }
                }
            } catch (e: Exception) {
                log(TAG, WARN) { "TTS init failed: $e" }
                if (!resumed) {
                    resumed = true
                    cont.resume(null)
                }
            }
        }
        engine = created
        created
    }

    suspend fun speak(text: String) {
        if (text.isBlank()) return
        try {
            val tts = engine() ?: return
            tts.language = Locale.getDefault()
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "andropods-${System.currentTimeMillis()}")
        } catch (e: Exception) {
            log(TAG, WARN) { "speak() failed: $e" }
        }
    }

    suspend fun speakBattery(device: PodDevice) {
        val parts = mutableListOf<String>()
        val left = device.batteryLeft.toPercent()
        val right = device.batteryRight.toPercent()
        val headset = device.batteryHeadset.toPercent()
        val case = device.batteryCase.toPercent()
        if (device.model.features.hasDualPods) {
            if (left != null) parts += context.getString(R.string.speech_battery_left, left)
            if (right != null) parts += context.getString(R.string.speech_battery_right, right)
        } else if (headset != null) {
            parts += context.getString(R.string.speech_battery_headset, headset)
        }
        if (case != null) parts += context.getString(R.string.speech_battery_case, case)
        if (parts.isEmpty()) return
        speak(context.getString(R.string.speech_battery_template, parts.joinToString(", ")))
    }

    private fun Float.toPercent(): Int? {
        if (this == BATTERY_UNKNOWN) return null
        return (this * 100).toInt().coerceIn(0, 100)
    }

    companion object {
        private val TAG = logTag("Common", "Speaker")
    }
}
