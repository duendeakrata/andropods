package eu.darken.capod.ui.fluid.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import eu.darken.capod.monitor.core.PodDevice
import kotlin.math.cos
import kotlin.math.sin

/**
 * PodsMate "Fluid Utility" BLE radar (F5).
 * Nearby (unmatched) pods as sonar dots: angle from address hash,
 * radius from Kalman-smoothed RSSI. Coexists with the flat unmatched list.
 */
@Composable
fun BleRadar(
    devices: List<PodDevice>,
    modifier: Modifier = Modifier,
) {
    val filters = remember { mutableMapOf<String, Kalman1D>() }
    val sweep by rememberInfiniteTransition(label = "radarSweep").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "angle",
    )

    val points = devices.mapNotNull { device ->
        val rssi = device.rssi
        if (rssi == 0) return@mapNotNull null
        val key = device.bleAddress ?: device.address ?: return@mapNotNull null
        val filter = filters.getOrPut(key) { Kalman1D(rssi.toFloat()) }
        RadarPoint(key = key, smoothedRssi = filter.update(rssi.toFloat()))
    }
    if (points.isEmpty()) return

    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outlineVariant

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
    ) {
        val side = minOf(maxWidth, maxHeight)
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = (minOf(size.width, size.height) / 2f) - 8.dp.toPx()
            // Range rings.
            listOf(0.33f, 0.66f, 1f).forEach { frac ->
                drawCircle(
                    color = outline,
                    radius = maxR * frac,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            // Sweep.
            val rad = Math.toRadians(sweep.toDouble())
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(primary.copy(alpha = 0f), primary.copy(alpha = 0.7f)),
                    start = Offset(cx, cy),
                    end = Offset(
                        cx + cos(rad).toFloat() * maxR,
                        cy + sin(rad).toFloat() * maxR,
                    ),
                ),
                start = Offset(cx, cy),
                end = Offset(
                    cx + cos(rad).toFloat() * maxR,
                    cy + sin(rad).toFloat() * maxR,
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            // Dots: closer (stronger RSSI) = nearer the center.
            points.forEach { point ->
                val closeness = 1f - ((-30f - point.smoothedRssi).coerceIn(0f, 70f) / 70f)
                val dist = maxR * (1f - closeness).coerceIn(0.08f, 1f)
                val angle = Math.toRadians((point.key.hashCode() % 360).toDouble())
                drawCircle(
                    color = primary.copy(alpha = 0.35f + 0.65f * closeness),
                    radius = (5.dp.toPx() + 4.dp.toPx() * closeness),
                    center = Offset(
                        cx + cos(angle).toFloat() * dist,
                        cy + sin(angle).toFloat() * dist,
                    ),
                )
            }
        }
    }
}

private data class RadarPoint(val key: String, val smoothedRssi: Float)

/** 1D Kalman filter for RSSI smoothing. */
private class Kalman1D(x0: Float) {
    private var x = x0
    private var p = 1f

    fun update(z: Float, q: Float = 0.5f, r: Float = 4f): Float {
        p += q
        val k = p / (p + r)
        x += k * (z - x)
        p *= (1f - k)
        return x
    }
}
