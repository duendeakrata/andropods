package com.andropods.gestures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Port de StemActionsConfig (CAPod) + presets por familia.
// En CAPod hasStemConfig=false bloquea la UI en modelos base;
// aquí `supportsAap` solo filtra acciones ANC, los taps siempre están disponibles.
@Serializable
data class GestureConfig(
    @SerialName("leftSingle") val leftSingle: GestureAction = GestureAction.PlayPause,
    @SerialName("leftDouble") val leftDouble: GestureAction = GestureAction.NextTrack,
    @SerialName("leftTriple") val leftTriple: GestureAction = GestureAction.PreviousTrack,
    @SerialName("leftLong") val leftLong: GestureAction = GestureAction.CycleAnc,
    @SerialName("rightSingle") val rightSingle: GestureAction = GestureAction.PlayPause,
    @SerialName("rightDouble") val rightDouble: GestureAction = GestureAction.NextTrack,
    @SerialName("rightTriple") val rightTriple: GestureAction = GestureAction.PreviousTrack,
    @SerialName("rightLong") val rightLong: GestureAction = GestureAction.ToggleAncTransparency,
) {
    // Misma máscara que StemConfigSender.toMask() en CAPod
    fun toAapMask(): Int {
        var mask = 0
        if (leftSingle !is GestureAction.None || rightSingle !is GestureAction.None) mask = mask or 0x01
        if (leftDouble !is GestureAction.None || rightDouble !is GestureAction.None) mask = mask or 0x02
        if (leftTriple !is GestureAction.None || rightTriple !is GestureAction.None) mask = mask or 0x04
        if (leftLong !is GestureAction.None || rightLong !is GestureAction.None) mask = mask or 0x08
        return mask
    }

    fun actionFor(side: Side, press: PressType): GestureAction = when (side) {
        Side.LEFT -> when (press) {
            PressType.SINGLE -> leftSingle
            PressType.DOUBLE -> leftDouble
            PressType.TRIPLE -> leftTriple
            PressType.LONG -> leftLong
        }
        Side.RIGHT -> when (press) {
            PressType.SINGLE -> rightSingle
            PressType.DOUBLE -> rightDouble
            PressType.TRIPLE -> rightTriple
            PressType.LONG -> rightLong
        }
    }

    enum class Side { LEFT, RIGHT }
    enum class PressType { SINGLE, DOUBLE, TRIPLE, LONG }

    companion object {
        fun defaultBase(): GestureConfig = GestureConfig(
            leftLong = GestureAction.VoiceAssistant,
            rightLong = GestureAction.VoiceAssistant,
        )
        fun defaultPro(): GestureConfig = GestureConfig()
    }
}
