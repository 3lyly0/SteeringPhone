package dev.steeringphone

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.steeringphone.network.ConnectionManager
import dev.steeringphone.network.ConnectionState
import dev.steeringphone.protocol.DrivePacket
import dev.steeringphone.protocol.ProtocolSerializer
import dev.steeringphone.sensor.SensorManagerWrapper
import dev.steeringphone.ui.connection.ConnectionScreen
import dev.steeringphone.ui.steering.SteeringScreen
import dev.steeringphone.ui.theme.RacingAccentCyan
import dev.steeringphone.ui.theme.RacingPrimaryRed
import dev.steeringphone.ui.theme.SteeringPhoneTheme
import dev.steeringphone.update.AppUpdateInfo
import dev.steeringphone.update.UpdateManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sensorManagerWrapper: SensorManagerWrapper

    @Inject
    lateinit var connectionManager: ConnectionManager

    @Inject
    lateinit var updateManager: UpdateManager

    private var sequenceNumber: UShort = 0u

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManagerWrapper.start()

        // Continuous telemetry streaming loop
        lifecycleScope.launch {
            sensorManagerWrapper.sensorDataFlow.collect { sensorData ->
                if (connectionManager.connectionState.value is ConnectionState.Connected) {
                    sequenceNumber++
                    val packet = DrivePacket(
                        sequenceNumber = sequenceNumber,
                        timestampUs = System.currentTimeMillis() * 1000L,
                        steeringAngle = sensorData.steeringAngle,
                        accelX = sensorData.accelX,
                        accelY = sensorData.accelY,
                        accelZ = sensorData.accelZ,
                        gyroZ = sensorData.gyroZ,
                        buttonMask = 0u,
                        throttle = 0u,
                        brake = 0u,
                        clutch = 0u,
                        batteryPercentage = 100u,
                        signalQuality = 100u,
                        pingMs = 1u
                    )
                    val bytes = ProtocolSerializer.serialize(packet)
                    connectionManager.sendPacket(bytes)
                }
            }
        }

        setContent {
            SteeringPhoneTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf("steering") }
                    var updateInfo by remember { mutableStateOf(AppUpdateInfo()) }
                    var isDownloadingUpdate by remember { mutableStateOf(false) }
                    var updateProgress by remember { mutableStateOf(0f) }

                    LaunchedEffect(Unit) {
                        lifecycleScope.launch {
                            val info = updateManager.checkForUpdate()
                            if (info.isUpdateAvailable) {
                                updateInfo = info
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        val sensorData by sensorManagerWrapper.sensorDataFlow.collectAsState()
                        val connectionState by connectionManager.connectionState.collectAsState()
                        val discoveredServers by connectionManager.discoveredServers.collectAsState()

                        val statusText = when (val state = connectionState) {
                            is ConnectionState.Connected -> "Connected (${state.transport})"
                            is ConnectionState.Connecting -> "Connecting..."
                            is ConnectionState.Discovering -> "Scanning..."
                            is ConnectionState.Failed -> "Failed"
                            else -> "Disconnected"
                        }

                        if (currentScreen == "steering") {
                            SteeringScreen(
                                sensorData = sensorData,
                                connectionStatusText = statusText,
                                onCalibrateCenter = { sensorManagerWrapper.calibrateCenter() },
                                onConnectClick = { currentScreen = "connection" }
                            )
                        } else {
                            ConnectionScreen(
                                discoveredServers = discoveredServers,
                                onScanClick = { connectionManager.startDiscovery() },
                                onConnectServer = { server ->
                                    connectionManager.connectUdp(server.ipAddress, server.port)
                                    currentScreen = "steering"
                                },
                                onManualConnectUdp = { ip, port ->
                                    connectionManager.connectUdp(ip, port)
                                    currentScreen = "steering"
                                },
                                onBackClick = { currentScreen = "steering" }
                            )
                        }

                        // Top GitHub Update Banner Notification
                        if (updateInfo.isUpdateAvailable) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .align(Alignment.TopCenter),
                                colors = CardDefaults.cardColors(containerColor = RacingPrimaryRed)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "New Update Available: v${updateInfo.latestVersion}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (isDownloadingUpdate) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { updateProgress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = RacingAccentCyan,
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { updateInfo = AppUpdateInfo() }) {
                                                Text("Later", color = Color.White)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    updateInfo.downloadUrl?.let { url ->
                                                        isDownloadingUpdate = true
                                                        lifecycleScope.launch {
                                                            updateManager.downloadAndInstall(
                                                                downloadUrl = url,
                                                                onProgress = { updateProgress = it },
                                                                onError = { err ->
                                                                    isDownloadingUpdate = false
                                                                    Toast.makeText(this@MainActivity, err, Toast.LENGTH_LONG).show()
                                                                }
                                                            )
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = RacingAccentCyan)
                                            ) {
                                                Text("Update Now", color = Color.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManagerWrapper.stop()
        connectionManager.disconnect()
    }
}
