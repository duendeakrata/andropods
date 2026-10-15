package eu.darken.capod.ui.fluid.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AirPodsUiStateTest : BaseTest() {

    @Test
    fun `dismiss popup clears flag and animation`() {
        val shown = AirPodsUiState(
            popupVisible = true,
            activeAnimations = setOf(AirPodsUiState.UiAnimation.POPUP_COUNT_UP),
        )
        val result = reduce(shown, FluidEvent.DismissPopup)
        assertFalse(result.popupVisible)
        assertTrue(result.activeAnimations.isEmpty())
    }

    @Test
    fun `show popup sets flag and animation`() {
        val result = reduce(AirPodsUiState(), FluidEvent.ShowPopup)
        assertTrue(result.popupVisible)
        assertEquals(
            setOf(AirPodsUiState.UiAnimation.POPUP_COUNT_UP),
            result.activeAnimations,
        )
    }

    @Test
    fun `engine events keep state instance`() {
        val state = AirPodsUiState(label = "Pods")
        assertEquals(state, reduce(state, FluidEvent.ToggleEarDetection(true)))
    }
}
