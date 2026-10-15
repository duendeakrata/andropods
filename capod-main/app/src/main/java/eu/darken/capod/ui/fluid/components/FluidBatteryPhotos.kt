package eu.darken.capod.ui.fluid.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.darken.capod.common.theming.fillColor
import eu.darken.capod.monitor.core.battery.BatteryTier
import eu.darken.capod.monitor.core.battery.batteryTier
import eu.darken.capod.pods.core.apple.ble.formatBatteryPercent
import eu.darken.capod.ui.fluid.theme.FluidTokens
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Validated prototype look: real device photos with a staggered drop-in,
 * level bars, count-up percentages. Unlinked slots render grayscale.
 */
data class PhotoSlot(
    val iconRes: Int,
    val label: String,
    val sublabel: String?,
    /** 0..1, -1f = unknown. */
    val percent: Float,
    val charging: Boolean,
    val linked: Boolean,
)

@Composable
fun FluidBatteryPhotos(
    slots: List<PhotoSlot>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(26.dp, Alignment.CenterHorizontally),
    ) {
        slots.forEachIndexed { index, slot ->
            PhotoSlotItem(slot = slot, index = index)
        }
    }
}

@Composable
private fun PhotoSlotItem(slot: PhotoSlot, index: Int) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(120L * index)
        entered = true
    }
    val t by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = FluidTokens.bouncy(),
        label = "podDrop",
    )
    val known = slot.percent >= 0f
    val countUp by animateIntAsState(
        targetValue = if (known && entered) (slot.percent * 100).roundToInt().coerceIn(0, 100) else 0,
        animationSpec = tween(900),
        label = "photoCountUp",
    )
    val barFrac by animateFloatAsState(
        targetValue = if (known && entered) slot.percent else 0f,
        animationSpec = tween(1100),
        label = "photoBar",
    )
    val context = LocalContext.current
    val density = LocalDensity.current
    val dropPx = with(density) { 26.dp.toPx() }
    val tier = batteryTier(slot.percent)
    val barColor = if (slot.linked && tier != BatteryTier.UNKNOWN) {
        tier.fillColor()
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            translationY = dropPx * (1f - t)
            val scale = 0.9f + 0.1f * t
            scaleX = scale
            scaleY = scale
            alpha = t
        },
    ) {
        Image(
            painter = painterResource(slot.iconRes),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            colorFilter = if (slot.linked) {
                null
            } else {
                ColorFilter.tint(Color.Gray, BlendMode.Saturation)
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        ) {
            if (known) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barFrac)
                        .background(barColor, CircleShape),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (slot.charging) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 2.dp),
                )
            }
            Text(
                text = if (known) {
                    "$countUp%"
                } else {
                    formatBatteryPercent(context, slot.percent)
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFeatureSettings = FluidTokens.TabularFigures,
                ),
            )
        }
        Text(
            text = slot.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (slot.sublabel != null) {
            Text(
                text = slot.sublabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
