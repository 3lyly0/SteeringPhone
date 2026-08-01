package dev.steeringphone.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles LAN auto-discovery of PC SteeringPhone host servers via UDP broadcast.
 */
@Singleton
class DiscoveryClient @Inject constructor() {

    /**
     * Broadcasts a discovery ping packet on port 45678 across all active local network interfaces and collects PC responses.
     *
     * @param timeoutMs Duration to listen for PC responses in milliseconds.
     * @return List of discovered servers on local subnet.
     */
    suspend fun scanForServers(timeoutMs: Int = DEFAULT_DISCOVERY_TIMEOUT_MS): List<DiscoveredServer> = withContext(Dispatchers.IO) {
        val servers = mutableListOf<DiscoveredServer>()
        var socket: DatagramSocket? = null

        try {
            socket = DatagramSocket().apply {
                broadcast = true
                soTimeout = 500
            }

            val requestData = DISCOVERY_REQUEST_MAGIC.toByteArray(Charsets.UTF_8)
            val addressesToPing = mutableSetOf<InetAddress>()
            
            try {
                addressesToPing.add(InetAddress.getByName(BROADCAST_ADDRESS))
            } catch (_: Exception) {}

            // Send to broadcast addresses of all active interfaces (e.g. 192.168.1.255)
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()
                    if (networkInterface.isLoopback || !networkInterface.isUp) continue

                    for (interfaceAddress in networkInterface.interfaceAddresses) {
                        val broadcast = interfaceAddress.broadcast
                        if (broadcast != null) {
                            addressesToPing.add(broadcast)
                        }
                    }
                }
            } catch (_: Exception) {}

            for (addr in addressesToPing) {
                try {
                    val sendPacket = DatagramPacket(requestData, requestData.size, addr, DISCOVERY_PORT)
                    socket.send(sendPacket)
                } catch (_: Exception) {}
            }

            val receiveBuffer = ByteArray(1024)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < timeoutMs) {
                try {
                    val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                    socket.receive(receivePacket)

                    val responseString = String(receivePacket.data, 0, receivePacket.length, Charsets.UTF_8).trim()
                    if (responseString.startsWith(DISCOVERY_RESPONSE_PREFIX)) {
                        // Format: STEERINGPHONE_PC:<hostname>:<port>:<transport>
                        val parts = responseString.split(":")
                        val hostname = if (parts.size > 1) parts[1] else "PC"
                        val port = if (parts.size > 2) parts[2].toIntOrNull() ?: DEFAULT_SERVER_PORT else DEFAULT_SERVER_PORT
                        val transportStr = if (parts.size > 3) parts[3] else "UDP"
                        val transport = if (transportStr.equals("WEBSOCKET", ignoreCase = true)) TransportType.WEBSOCKET else TransportType.UDP

                        val server = DiscoveredServer(
                            hostname = hostname,
                            ipAddress = receivePacket.address.hostAddress ?: "127.0.0.1",
                            port = port,
                            transport = transport
                        )
                        if (servers.none { it.ipAddress == server.ipAddress && it.port == server.port }) {
                            servers.add(server)
                        }
                    }
                } catch (_: SocketTimeoutException) {
                    continue
                }
            }
        } catch (_: Exception) {
            // Ignore socket error during scan
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }

        servers
    }

    companion object {
        const val DISCOVERY_PORT: Int = 45678
        const val DEFAULT_SERVER_PORT: Int = 45680
        const val BROADCAST_ADDRESS: String = "255.255.255.255"
        const val DISCOVERY_REQUEST_MAGIC: String = "STEERINGPHONE_DISCOVER"
        const val DISCOVERY_RESPONSE_PREFIX: String = "STEERINGPHONE_PC"
        const val DEFAULT_DISCOVERY_TIMEOUT_MS: Int = 2000
    }
}
