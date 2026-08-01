package dev.steeringphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.steeringphone.network.ConnectionManager
import dev.steeringphone.network.ConnectionState
import dev.steeringphone.protocol.DrivePacket
import dev.steeringphone.protocol.ProtocolSerializer
import dev.steeringphone.sensor.SensorManagerWrapper
import dev.steeringphone.ui.connection.ConnectionScreen
import dev.steeringphone.ui.steering.SteeringScreen
import dev.steeringphone.ui.theme.SteeringPhoneTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sensorManagerWrapper: SensorManagerWrapper

    @Inject
    lateinit var connectionManager: ConnectionManager

    private var sequenceNumber: UShort = 0u

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManagerWrapper.start()

        // Continuous packet streaming coroutine loop
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
