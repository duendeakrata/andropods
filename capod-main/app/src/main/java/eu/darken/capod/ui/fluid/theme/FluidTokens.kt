package eu.darken.capod.ui.fluid.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp

/**
 * PodsMate "Fluid Utility" design tokens (F1).
 * Physics, radii and haptics shared by all fluid components.
 */
object FluidTokens {
    val CardRadius = 28.dp
    val SheetRadius = 32.dp
    val GaugeSize = 208.dp

    /** Tabular figures so battery percentages don't jitter: fontFeatureSettings = "tnum". */
    const val TabularFigures = "tnum"

    fun <T> bouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun <T> snappy() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}

/** Centralized haptics so feedback stays consistent (and stays best-effort). */
object FluidHaptics {
    fun confirm(feedback: HapticFeedback) {
        runCatching { feedback.performHapticFeedback(HapticFeedbackType.LongPress) }
    }

    fun tick(feedback: HapticFeedback) {
        runCatching { feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
    }
}
