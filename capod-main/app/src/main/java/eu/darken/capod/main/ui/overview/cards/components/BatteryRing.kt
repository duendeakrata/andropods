package eu.darken.capod.main.ui.overview.cards.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.darken.capod.common.compose.Preview2
import eu.darken.capod.common.compose.PreviewWrapper
import eu.darken.capod.common.theming.fillColor
import eu.darken.capod.monitor.core.battery.BatteryTier
import eu.darken.capod.monitor.core.battery.batteryTier
import eu.darken.capod.pods.core.apple.ble.batteryProgress
import eu.darken.capod.pods.core.apple.ble.formatBatteryPercent

/**
 * Andropods addition: circular battery gauge with the percent in the middle,
 * matching the pod rings in [eu.darken.capod.main.ui.overview.cards.DualPodsCard].
 */
@Composable
fun BatteryRing(
    percent: Float,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    strokeWidth: Dp = 6.dp,
) {
    val context = LocalContext.current
    val tier = batteryTier(percent)
    val isKnown = tier != BatteryTier.UNKNOWN
    val animatedProgress by animateFloatAsState(
        targetValue = batteryProgress(percent),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "ringProgress",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = tier.fillColor()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
            )
            if (isKnown) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = stroke,
                )
            }
        }
        Text(
            text = formatBatteryPercent(context, percent),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview2
@Composable
private fun BatteryRingPreview() = PreviewWrapper {
    BatteryRing(percent = 0.72f)
}
