package eu.darken.capod.ui.fluid.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.darken.capod.common.theming.fillColor
import eu.darken.capod.monitor.core.battery.BatteryTier
import eu.darken.capod.monitor.core.battery.batteryTier
import eu.darken.capod.pods.core.apple.ble.batteryProgress
import eu.darken.capod.pods.core.apple.ble.formatBatteryPercent
import eu.darken.capod.ui.fluid.state.AirPodsUiState
import eu.darken.capod.ui.fluid.theme.FluidTokens

/**
 * PodsMate "Fluid Utility" battery HUD (F3).
 * Concentric spring-settled rings (L/R/case) on a single Canvas + legend.
 * Charging slots breathe via a shared light pulse; unknown slots render dashed.
 */
@Composable
fun BatteryStatusRingGroup(
    state: AirPodsUiState,
    leftLabel: String,
    rightLabel: String,
    caseLabel: String,
    leftEta: String?,
    rightEta: String?,
    caseEta: String?,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedLeft by animateFloatAsState(
        targetValue = batteryProgress(state.left.level),
        animationSpec = FluidTokens.bouncy(),
        label = "ringLeft",
    )
    val animatedRight by animateFloatAsState(
        targetValue = batteryProgress(state.right.level),
        animationSpec = FluidTokens.bouncy(),
        label = "ringRight",
    )
    val animatedCase by animateFloatAsState(
        targetValue = batteryProgress(state.case.level),
        animationSpec = FluidTokens.bouncy(),
        label = "ringCase",
    )
    val pulse by rememberInfiniteTransition(label = "chargePulse").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400)),
        label = "pulse",
    )

    val slots = buildList {
        add(RingSlot(state.left.level, animatedLeft, state.left.charging))
        if (state.hasDualPods) add(RingSlot(state.right.level, animatedRight, state.right.charging))
        if (state.case.level >= 0f || state.hasDualPods) {
            add(RingSlot(state.case.level, animatedCase, state.case.charging))
        }
    }

    Column(
        modifier = modifier
            .alpha(if (dimmed) 0.7f else 1f)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RingCanvas(slots = slots, pulse = pulse)
        Spacer(modifier = Modifier.padding(top = 4.dp))
        SlotLegendRow(
            dotColor = slotColor(state.left.level),
            label = leftLabel,
            level = state.left.level,
            eta = leftEta,
            charging = state.left.charging,
            inEar = state.left.inEar,
        )
        if (state.hasDualPods) {
            SlotLegendRow(
                dotColor = slotColor(state.right.level),
                label = rightLabel,
                level = state.right.level,
                eta = rightEta,
                charging = state.right.charging,
                inEar = state.right.inEar,
            )
        }
        if (state.case.level >= 0f || state.hasDualPods) {
            SlotLegendRow(
                dotColor = slotColor(state.case.level),
                label = caseLabel,
                level = state.case.level,
                eta = caseEta,
                charging = state.case.charging,
                inEar = null,
            )
        }
    }
}

private data class RingSlot(val level: Float, val animated: Float, val charging: Boolean)

@Composable
private fun slotColor(level: Float) = if (batteryTier(level) == BatteryTier.UNKNOWN) {
    MaterialTheme.colorScheme.surfaceVariant
} else {
    batteryTier(level).fillColor()
}

@Composable
private fun RingCanvas(slots: List<RingSlot>, pulse: Float) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val size = FluidTokens.GaugeSize
    val stroke = 13.dp
    val gap = 7.dp
    // Colors must be resolved outside the draw scope (fillColor() is @Composable).
    data class ArcStyle(val known: Boolean, val color: androidx.compose.ui.graphics.Color)
    val styles = slots.map { slot ->
        val tier = batteryTier(slot.level)
        val known = tier != BatteryTier.UNKNOWN
        ArcStyle(known, if (known) tier.fillColor() else track)
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = stroke.toPx()
            val gapPx = gap.toPx()
            var inset = 0f
            val diameter = size.toPx()
            slots.forEachIndexed { index, slot ->
                val style = styles[index]
                val box = androidx.compose.ui.geometry.Size(diameter - inset * 2f, diameter - inset * 2f)
                val origin = androidx.compose.ui.geometry.Offset(inset, inset)
                drawArc(
                    color = track,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = origin,
                    size = box,
                    style = Stroke(
                        width = strokePx,
                        cap = StrokeCap.Round,
                        pathEffect = if (style.known) null else PathEffect.dashPathEffect(floatArrayOf(9f, 9f)),
                    ),
                )
                if (style.known) {
                    val alpha = if (slot.charging) pulse else 1f
                    if (slot.charging) {
                        // Halo glow behind the progress arc.
                        drawArc(
                            color = style.color.copy(alpha = 0.25f * pulse),
                            startAngle = -90f,
                            sweepAngle = 360f * slot.animated,
                            useCenter = false,
                            topLeft = origin,
                            size = box,
                            style = Stroke(width = strokePx * 1.9f, cap = StrokeCap.Round),
                        )
                    }
                    drawArc(
                        color = style.color.copy(alpha = alpha),
                        startAngle = -90f,
                        sweepAngle = 360f * slot.animated,
                        useCenter = false,
                        topLeft = origin,
                        size = box,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                }
                inset += strokePx + gapPx
            }
        }
        val lowest = slots.mapNotNull { it.level.takeIf { l -> l >= 0f } }.minOrNull()
        if (lowest != null) {
            val context = LocalContext.current
            Text(
                text = formatBatteryPercent(context, lowest),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFeatureSettings = FluidTokens.TabularFigures,
                ),
            )
        }
    }
}

@Composable
private fun SlotLegendRow(
    dotColor: androidx.compose.ui.graphics.Color,
    label: String,
    level: Float,
    eta: String?,
    charging: Boolean,
    inEar: Boolean?,
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dotColor, CircleShape),
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        if (inEar == true) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (charging) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
        }
        if (eta != null) {
            Text(
                text = eta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = formatBatteryPercent(context, level),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFeatureSettings = FluidTokens.TabularFigures,
            ),
        )
    }
}
