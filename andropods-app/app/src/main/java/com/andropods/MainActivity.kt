package com.andropods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andropods.gestures.GestureConfig
import com.andropods.ui.GesturesScreen
import com.andropods.ui.theme.AndropodsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndropodsTheme {
                var tab by remember { mutableIntStateOf(0) }
                var gestures by remember { mutableStateOf(GestureConfig.defaultBase()) }
                var supportsAnc by remember { mutableStateOf(false) }
                Scaffold(bottomBar = {
                    NavigationBar {
                        NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Batería") }, icon = { Text("🔋") })
                        NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Gestos") }, icon = { Text("👆") })
                    }
                }) { pad ->
                    Box(Modifier.padding(pad)) {
                        if (tab == 0) DashboardScreen(supportsAnc) { supportsAnc = it }
                        else GesturesScreen(gestures, supportsAnc) { gestures = it }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(supportsAnc: Boolean, onSupportsAnc: (Boolean) -> Unit) {
    var left by remember { mutableFloatStateOf(0.85f) }
    var right by remember { mutableFloatStateOf(0.72f) }
    var case by remember { mutableFloatStateOf(0.60f) }
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Andropods", style = MaterialTheme.typography.headlineMedium)
        Text("Toca los anillos para simular nivel (BLE real en fase 2)", style = MaterialTheme.typography.bodySmall)
        BatteryRing("Izquierdo", left) { left = it }
        BatteryRing("Derecho", right) { right = it }
        BatteryRing("Case", case) { case = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !supportsAnc, onClick = { onSupportsAnc(false) }, label = { Text("AirPods base") })
            FilterChip(selected = supportsAnc, onClick = { onSupportsAnc(true) }, label = { Text("AirPods Pro (ANC)") })
        }
    }
}

@Composable
fun BatteryRing(name: String, level: Float, onChange: (Float) -> Unit) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text("${(level * 100).toInt()}%")
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { level }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Slider(value = level, onValueChange = onChange)
        }
    }
}
