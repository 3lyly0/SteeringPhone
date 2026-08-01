package dev.steeringphone.ui.steering

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.steeringphone.sensor.SensorData
import dev.steeringphone.ui.theme.RacingAccentCyan
import dev.steeringphone.ui.theme.RacingDarkBg
import dev.steeringphone.ui.theme.RacingPrimaryRed

@Composable
fun SteeringScreen(
    sensorData: SensorData,
    connectionStatusText: String,
    onCalibrateCenter: () -> Unit,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedAngle by animateFloatAsState(targetValue = sensorData.fusedAngleDegrees, label = "steeringAngle")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RacingDarkBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header info bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEERINGPHONE",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = RacingPrimaryRed
                )
            )
            Button(
                onClick = onConnectClick,
                colors = ButtonDefaults.buttonColors(containerColor = RacingAccentCyan)
            ) {
                Text(text = connectionStatusText, color = Color.Black, fontSize = 12.sp)
            }
        }

        // Live Steering Wheel Canvas Visualizer
        Box(
            modifier = Modifier
                .size(240.dp)
                .rotate(animatedAngle),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                drawCircle(
                    color = RacingAccentCyan,
                    style = Stroke(width = strokeWidth)
                )
                // Center marker notch
                drawRect(
                    color = RacingPrimaryRed,
                    size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 32.dp.toPx()),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        (size.width - 12.dp.toPx()) / 2,
                        0f
                    )
                )
            }
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = RacingPrimaryRed
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("SP", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Telemetry Metrics
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f°", sensorData.fusedAngleDegrees),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = String.format("Output: %.2f", sensorData.steeringAngle),
                style = MaterialTheme.typography.bodyLarge.copy(color = RacingAccentCyan)
            )
        }

        // Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onCalibrateCenter) {
                Text("Zero Center", color = Color.White)
            }
        }
    }
}
