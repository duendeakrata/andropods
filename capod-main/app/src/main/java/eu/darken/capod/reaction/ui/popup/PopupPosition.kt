package eu.darken.capod.reaction.ui.popup

import eu.darken.capod.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Andropods addition: user-configurable case-popup position. */
@Serializable
enum class PopupPosition(val labelRes: Int) {
    @SerialName("TOP") TOP(R.string.popup_position_top),
    @SerialName("CENTER") CENTER(R.string.popup_position_center),
    @SerialName("BOTTOM") BOTTOM(R.string.popup_position_bottom),
}
