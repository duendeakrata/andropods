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
            var tts: TextToSpeech? = null
            var resumed = false
            try {
                tts = TextToSpeech(context) { status ->
                    if (resumed) return@TextToSpeech
                    resumed = true
                    val ready = tts
                    if (status == TextToSpeech.SUCCESS && ready != null) {
                        cont.resume(ready)
                    } else {
                        runCatching { ready?.shutdown() }
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
        speakBattery(
            isDualPods = device.model.features.hasDualPods,
            left = device.batteryLeft.takeUnless { it == BATTERY_UNKNOWN },
            right = device.batteryRight.takeUnless { it == BATTERY_UNKNOWN },
            headset = device.batteryHeadset.takeUnless { it == BATTERY_UNKNOWN },
            case = device.batteryCase.takeUnless { it == BATTERY_UNKNOWN },
        )
    }

    suspend fun speakBattery(
        isDualPods: Boolean,
        left: Float?,
        right: Float?,
        headset: Float?,
        case: Float?,
    ) {
        val parts = mutableListOf<String>()
        if (isDualPods) {
            left?.toPercent()?.let { parts += context.getString(R.string.speech_battery_left, it) }
            right?.toPercent()?.let { parts += context.getString(R.string.speech_battery_right, it) }
        } else {
            headset?.toPercent()?.let { parts += context.getString(R.string.speech_battery_headset, it) }
        }
        case?.toPercent()?.let { parts += context.getString(R.string.speech_battery_case, it) }
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
