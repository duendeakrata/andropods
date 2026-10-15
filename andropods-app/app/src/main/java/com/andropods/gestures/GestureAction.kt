package com.andropods.gestures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Port extendido de eu.darken.capod.reaction.core.stem.StemAction
// + 4 acciones nuevas que CAPod no tiene.
@Serializable
sealed interface GestureAction {
    @Serializable @SerialName("NONE") data object None : GestureAction
    @Serializable @SerialName("NO_ACTION") data object NoAction : GestureAction
    @Serializable @SerialName("PLAY_PAUSE") data object PlayPause : GestureAction
    @Serializable @SerialName("NEXT_TRACK") data object NextTrack : GestureAction
    @Serializable @SerialName("PREVIOUS_TRACK") data object PreviousTrack : GestureAction
    @Serializable @SerialName("VOLUME_UP") data object VolumeUp : GestureAction
    @Serializable @SerialName("VOLUME_DOWN") data object VolumeDown : GestureAction
    @Serializable @SerialName("STOP") data object Stop : GestureAction
    @Serializable @SerialName("FAST_FORWARD") data object FastForward : GestureAction
    @Serializable @SerialName("REWIND") data object Rewind : GestureAction
    @Serializable @SerialName("MUTE_TOGGLE") data object MuteToggle : GestureAction
    @Serializable @SerialName("CYCLE_ANC") data object CycleAnc : GestureAction
    @Serializable @SerialName("TOGGLE_ANC_TRANSPARENCY") data object ToggleAncTransparency : GestureAction
    // Nuevas en Andropods:
    @Serializable @SerialName("VOICE_ASSISTANT") data object VoiceAssistant : GestureAction
    @Serializable @SerialName("OPEN_APP") data class OpenApp(val packageName: String) : GestureAction
    @Serializable @SerialName("READ_NOTIFICATIONS") data object ReadNotifications : GestureAction
    @Serializable @SerialName("LOW_LATENCY_TOGGLE") data object LowLatencyToggle : GestureAction

    companion object {
        val mediaOnly: List<GestureAction> = listOf(
            None, PlayPause, NextTrack, PreviousTrack, VolumeUp, VolumeDown,
            MuteToggle, Stop, FastForward, Rewind, NoAction,
        )
        val all: List<GestureAction> = mediaOnly + listOf(
            CycleAnc, ToggleAncTransparency,
            VoiceAssistant, ReadNotifications, LowLatencyToggle,
        )
    }
}
