package eu.darken.capod.ui.fluid.state

import eu.darken.capod.monitor.core.PodDevice
import eu.darken.capod.pods.core.apple.PodModel
import eu.darken.capod.pods.core.apple.aap.protocol.AapSetting
import eu.darken.capod.reaction.core.stem.StemAction
import eu.darken.capod.reaction.core.stem.StemActionsConfig

/**
 * PodsMate "Fluid Utility" UI state (F2).
 * Single immutable snapshot per device screen, mapped from the engine's [PodDevice].
 * The engine is never imported by composables — only this state.
 */
data class AirPodsUiState(
    val label: String = "",
    val model: PodModel = PodModel.UNKNOWN,
    val connection: Connection = Connection.Idle,
    val left: BudState = BudState(),
    val right: BudState = BudState(),
    val case: CaseState = CaseState(),
    val hasDualPods: Boolean = true,
    val popupVisible: Boolean = false,
    val activeAnimations: Set<UiAnimation> = emptySet(),
) {
    enum class Connection { Idle, Nearby, Connecting, Live, Stale }

    data class BudState(
        /** 0..1, -1f = unknown (mirrors engine convention). */
        val level: Float = -1f,
        val charging: Boolean = false,
        /** null = no ear detection on this model. */
        val inEar: Boolean? = null,
        val minutesLeft: Int? = null,
    )

    data class CaseState(
        val level: Float = -1f,
        val charging: Boolean = false,
        val lidOpen: Boolean? = null,
    )

    enum class UiAnimation { CHARGING_PULSE, LOW_BATTERY_NUDGE, POPUP_COUNT_UP }
}

sealed interface FluidEvent {
    data class ToggleEarDetection(val enabled: Boolean) : FluidEvent
    data class SetAncMode(val mode: AapSetting.AncMode.Value) : FluidEvent
    data class SelectGesture(
        val side: StemActionsConfigSide,
        val press: StemPressType,
        val action: StemAction,
    ) : FluidEvent
    data object DismissPopup : FluidEvent
    data object ShowPopup : FluidEvent

    enum class StemActionsConfigSide { LEFT, RIGHT }
    enum class StemPressType { SINGLE, DOUBLE, TRIPLE, LONG }
}

/** Pure reducer: UI-optimistic transitions only, engine truth always wins on next emission. */
fun reduce(state: AirPodsUiState, event: FluidEvent): AirPodsUiState = when (event) {
    is FluidEvent.DismissPopup -> state.copy(
        popupVisible = false,
        activeAnimations = state.activeAnimations - AirPodsUiState.UiAnimation.POPUP_COUNT_UP,
    )
    is FluidEvent.ShowPopup -> state.copy(
        popupVisible = true,
        activeAnimations = state.activeAnimations + AirPodsUiState.UiAnimation.POPUP_COUNT_UP,
    )
    is FluidEvent.ToggleEarDetection,
    is FluidEvent.SetAncMode,
    is FluidEvent.SelectGesture,
        -> state // executed as side effects by the ViewModel; state refreshes from the engine
}

/** Maps engine truth to UI state. Label is resolved by the caller (needs Context). */
fun PodDevice.toFluidState(
    label: String,
    leftMinutes: Int? = null,
    rightMinutes: Int? = null,
): AirPodsUiState {
    val anyCharging = isLeftPodCharging == true || isRightPodCharging == true || isCaseCharging == true
    return AirPodsUiState(
        label = label,
        model = model,
        connection = when {
            isLive -> AirPodsUiState.Connection.Live
            address != null -> AirPodsUiState.Connection.Connecting
            else -> AirPodsUiState.Connection.Nearby
        },
        left = AirPodsUiState.BudState(
            level = batteryLeft,
            charging = isLeftPodCharging == true,
            inEar = isLeftInEar,
            minutesLeft = leftMinutes,
        ),
        right = AirPodsUiState.BudState(
            level = batteryRight,
            charging = isRightPodCharging == true,
            inEar = isRightInEar,
            minutesLeft = rightMinutes,
        ),
        case = AirPodsUiState.CaseState(
            level = batteryCase,
            charging = isCaseCharging == true,
            lidOpen = null,
        ),
        hasDualPods = hasDualPods,
        activeAnimations = if (anyCharging) {
            setOf(AirPodsUiState.UiAnimation.CHARGING_PULSE)
        } else {
            emptySet()
        },
    )
}
