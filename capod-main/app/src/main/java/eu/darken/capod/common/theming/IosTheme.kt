package eu.darken.capod.common.theming

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * PodsMate identity: iOS Settings inspired palette applied on top of the
 * user-selected [ThemeColor] scheme. Only the structural roles (backgrounds,
 * surfaces) are remapped to the iOS grouped look; the accent (primary) keeps
 * following the user's color choice.
 */
object IosTheme {

    private val IosGroupedLight = Color(0xFFF2F2F7)
    private val IosCardLight = Color(0xFFFFFFFF)
    private val IosGroupedDark = Color(0xFF000000)
    private val IosCardDark = Color(0xFF1C1C1E)
    private val IosFillDark = Color(0xFF2C2C2E)
    private val IosBlue = Color(0xFF007AFF)
    private val IosBlueDark = Color(0xFF0A84FF)

    fun adapt(scheme: ColorScheme, darkTheme: Boolean): ColorScheme = if (darkTheme) {
        scheme.copy(
            primary = IosBlueDark,
            onPrimary = Color.White,
            primaryContainer = IosBlueDark,
            onPrimaryContainer = Color.White,
            background = IosGroupedDark,
            onBackground = Color.White,
            surface = IosCardDark,
            onSurface = Color.White,
            surfaceVariant = IosFillDark,
            onSurfaceVariant = Color(0xFFAEAEB2),
            surfaceDim = IosGroupedDark,
            surfaceBright = IosCardDark,
            surfaceContainerLowest = IosCardDark,
            surfaceContainerLow = IosCardDark,
            surfaceContainer = IosCardDark,
            surfaceContainerHigh = IosCardDark,
            surfaceContainerHighest = IosFillDark,
            outlineVariant = Color(0xFF38383A),
        )
    } else {
        scheme.copy(
            primary = IosBlue,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDBE9FF),
            onPrimaryContainer = IosBlue,
            background = IosGroupedLight,
            onBackground = Color.Black,
            surface = IosCardLight,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFE9E9EE),
            onSurfaceVariant = Color(0xFF6B6B70),
            surfaceDim = IosGroupedLight,
            surfaceBright = IosCardLight,
            surfaceContainerLowest = IosCardLight,
            surfaceContainerLow = IosCardLight,
            surfaceContainer = IosCardLight,
            surfaceContainerHigh = IosCardLight,
            surfaceContainerHighest = Color(0xFFE9E9EE),
            outlineVariant = Color(0xFFD1D1D6),
        )
    }
}
