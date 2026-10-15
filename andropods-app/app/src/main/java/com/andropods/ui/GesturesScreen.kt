package com.andropods.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andropods.gestures.GestureAction
import com.andropods.gestures.GestureConfig

// Reemplazo M3 Expressive de PressControlsScreen + PressMappingsCard de CAPod.
// Muestra los 8 slots siempre (CAPod los oculta si hasStemConfig==false).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturesScreen(
    config: GestureConfig,
    supportsAnc: Boolean,
    onChange: (GestureConfig) -> Unit,
) {
    var editing by remember { mutableStateOf<Pair<GestureConfig.Side, GestureConfig.PressType>?>(null) }
    val actions = remember(supportsAnc) {
        if (supportsAnc) GestureAction.all else GestureAction.mediaOnly + listOf(
            GestureAction.VoiceAssistant, GestureAction.ReadNotifications, GestureAction.LowLatencyToggle
        )
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Gestos") }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GestureConfig.Side.entries.forEach { side ->
                item {
                    Text(
                        if (side == GestureConfig.Side.LEFT) "AirPod izquierdo" else "AirPod derecho",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                GestureConfig.PressType.entries.forEach { press ->
                    val current = config.actionFor(side, press)
                    item {
                        ElevatedCard(onClick = { editing = side to press }) {
                            ListItem(
                                headlineContent = { Text(labelFor(press)) },
                                supportingContent = { Text(nameFor(current)) },
                                trailingContent = { Text(emojiFor(current)) },
                            )
                        }
                    }
                }
            }
        }
    }
    editing?.let { (side, press) ->
        var sel by remember { mutableStateOf(config.actionFor(side, press)) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("${labelFor(press)} — ${if (side == GestureConfig.Side.LEFT) "izq." else "der."}") },
            text = {
                Column {
                    actions.forEach { a ->
                        FilterChip(
                            selected = a::class == sel::class,
                            onClick = { sel = a },
                            label = { Text("${emojiFor(a)} ${nameFor(a)}") },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(applyTo(config, side, press, sel)); editing = null
                }) { Text("Guardar") }
            },
        )
    }
}

private fun applyTo(c: GestureConfig, s: GestureConfig.Side, p: GestureConfig.PressType, a: GestureAction) = when (s) {
    GestureConfig.Side.LEFT -> when (p) {
        GestureConfig.PressType.SINGLE -> c.copy(leftSingle = a)
        GestureConfig.PressType.DOUBLE -> c.copy(leftDouble = a)
        GestureConfig.PressType.TRIPLE -> c.copy(leftTriple = a)
        GestureConfig.PressType.LONG -> c.copy(leftLong = a)
    }
    GestureConfig.Side.RIGHT -> when (p) {
        GestureConfig.PressType.SINGLE -> c.copy(rightSingle = a)
        GestureConfig.PressType.DOUBLE -> c.copy(rightDouble = a)
        GestureConfig.PressType.TRIPLE -> c.copy(rightTriple = a)
        GestureConfig.PressType.LONG -> c.copy(rightLong = a)
    }
}

private fun labelFor(p: GestureConfig.PressType) = when (p) {
    GestureConfig.PressType.SINGLE -> "1 toque / squeeze"
    GestureConfig.PressType.DOUBLE -> "2 toques / squeezes"
    GestureConfig.PressType.TRIPLE -> "3 toques / squeezes"
    GestureConfig.PressType.LONG -> "Mantener (ANC en Pro)"
}
private fun nameFor(a: GestureAction) = when (a) {
    is GestureAction.None -> "Sin configurar"
    is GestureAction.NoAction -> "Sin acción"
    is GestureAction.PlayPause -> "Play / Pausa"
    is GestureAction.NextTrack -> "Siguiente"
    is GestureAction.PreviousTrack -> "Anterior"
    is GestureAction.VolumeUp -> "Subir volumen"
    is GestureAction.VolumeDown -> "Bajar volumen"
    is GestureAction.Stop -> "Stop"
    is GestureAction.FastForward -> "Avanzar"
    is GestureAction.Rewind -> "Retroceder"
    is GestureAction.MuteToggle -> "Mutear"
    is GestureAction.CycleAnc -> "Ciclar ANC"
    is GestureAction.ToggleAncTransparency -> "ANC ↔ Transparencia"
    is GestureAction.VoiceAssistant -> "Asistente de voz"
    is GestureAction.OpenApp -> "Abrir app"
    is GestureAction.ReadNotifications -> "Leer notificaciones"
    is GestureAction.LowLatencyToggle -> "Modo juego"
}
private fun emojiFor(a: GestureAction) = when (a) {
    is GestureAction.PlayPause -> "▶️"
    is GestureAction.NextTrack -> "⏭️"
    is GestureAction.PreviousTrack -> "⏮️"
    is GestureAction.VolumeUp -> "🔊"
    is GestureAction.VolumeDown -> "🔉"
    is GestureAction.CycleAnc -> "🔇"
    is GestureAction.VoiceAssistant -> "🎙️"
    else -> "⚙️"
}
