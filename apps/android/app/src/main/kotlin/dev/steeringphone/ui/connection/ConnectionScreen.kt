package dev.steeringphone.ui.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.steeringphone.network.DiscoveredServer
import dev.steeringphone.ui.theme.RacingAccentCyan
import dev.steeringphone.ui.theme.RacingCardBg
import dev.steeringphone.ui.theme.RacingDarkBg
import dev.steeringphone.ui.theme.RacingPrimaryRed

@Composable
fun ConnectionScreen(
    discoveredServers: List<DiscoveredServer>,
    onScanClick: () -> Unit,
    onConnectServer: (DiscoveredServer) -> Unit,
    onManualConnectUdp: (host: String, port: Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var manualIp by remember { mutableStateOf("192.168.1.100") }
    var manualPort by remember { mutableStateOf("45679") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RacingDarkBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text("< Back", color = RacingAccentCyan)
            }
            Text(
                text = "CONNECTION SETUP",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = RacingPrimaryRed)
            ) {
                Text("Scan LAN", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Discovered Host PCs", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (discoveredServers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RacingCardBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No servers found. Tap 'Scan LAN' or connect manually.",
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(discoveredServers) { server ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConnectServer(server) },
                        colors = CardDefaults.cardColors(containerColor = RacingCardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(server.hostname, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${server.ipAddress}:${server.port}", color = RacingAccentCyan)
                            }
                            Button(
                                onClick = { onConnectServer(server) },
                                colors = ButtonDefaults.buttonColors(containerColor = RacingAccentCyan)
                            ) {
                                Text("Connect", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual Connection Form
        Text("Manual IP Connection", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = manualIp,
            onValueChange = { manualIp = it },
            label = { Text("PC IP Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val portInt = manualPort.toIntOrNull() ?: 45679
                onManualConnectUdp(manualIp.trim(), portInt)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RacingPrimaryRed)
        ) {
            Text("Connect to IP", color = Color.White)
        }
    }
}
