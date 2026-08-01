package dev.steeringphone.ui.pedals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.steeringphone.ui.theme.RacingAccentCyan
import dev.steeringphone.ui.theme.RacingDarkBg
import dev.steeringphone.ui.theme.RacingGreenOk
import dev.steeringphone.ui.theme.RacingPrimaryRed

@Composable
fun PedalsScreen(
    onThrottleChange: (UByte) -> Unit,
    onBrakeChange: (UByte) -> Unit,
    modifier: Modifier = Modifier
) {
    var throttleVal by remember { mutableStateOf(0f) }
    var brakeVal by remember { mutableStateOf(0f) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(RacingDarkBg)
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brake Slider Pedal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text("BRAKE", color = RacingPrimaryRed, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = brakeVal,
                onValueChange = {
                    brakeVal = it
                    onBrakeChange((it * 255).toInt().toUByte())
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = RacingPrimaryRed,
                    activeTrackColor = RacingPrimaryRed
                )
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Throttle Slider Pedal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text("THROTTLE", color = RacingGreenOk, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = throttleVal,
                onValueChange = {
                    throttleVal = it
                    onThrottleChange((it * 255).toInt().toUByte())
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = RacingGreenOk,
                    activeTrackColor = RacingGreenOk
                )
            )
        }
    }
}
