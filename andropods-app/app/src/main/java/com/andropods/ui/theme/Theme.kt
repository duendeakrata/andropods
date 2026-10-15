package com.andropods.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Light = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1B6EF3),
    secondary = androidx.compose.ui.graphics.Color(0xFF7C4DFF),
    tertiary = androidx.compose.ui.graphics.Color(0xFF00BCD4),
)
private val Dark = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF82B4FF),
    secondary = androidx.compose.ui.graphics.Color(0xFFB388FF),
    tertiary = androidx.compose.ui.graphics.Color(0xFF4DD0E1),
)

@Composable
fun AndropodsTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
