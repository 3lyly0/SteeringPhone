package dev.steeringphone.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConnectionManagerTest {

    private lateinit var udpClient: UdpClient
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var discoveryClient: DiscoveryClient
    private lateinit var connectionManager: ConnectionManager

    @Before
    fun setUp() {
        udpClient = UdpClient()
        webSocketClient = WebSocketClient()
        discoveryClient = DiscoveryClient()
        connectionManager = ConnectionManager(udpClient, webSocketClient, discoveryClient)
    }

    @Test
    fun testInitialStateIsDisconnected() {
        val state = connectionManager.connectionState.value
        assertTrue(state is ConnectionState.Disconnected)
    }

    @Test
    fun testDiscoveredServerModel() {
        val server = DiscoveredServer(
            hostname = "DESKTOP-SIMRACING",
            ipAddress = "192.168.1.100",
            port = 45679,
            transport = TransportType.UDP
        )

        assertEquals("DESKTOP-SIMRACING", server.hostname)
        assertEquals("192.168.1.100", server.ipAddress)
        assertEquals(45679, server.port)
        assertEquals(TransportType.UDP, server.transport)
    }

    @Test
    fun testConnectionStateTypes() {
        val connectedState = ConnectionState.Connected(TransportType.UDP, "192.168.1.50", 45679)
        assertEquals(TransportType.UDP, connectedState.transport)
        assertEquals("192.168.1.50", connectedState.address)
        assertEquals(45679, connectedState.port)

        val failedState = ConnectionState.Failed("Connection timeout")
        assertEquals("Connection timeout", failedState.reason)
    }
}
