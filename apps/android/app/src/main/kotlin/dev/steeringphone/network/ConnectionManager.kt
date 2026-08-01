package dev.steeringphone.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates connection state machine, transport switching, discovery, and packet transmission.
 */
@Singleton
class ConnectionManager @Inject constructor(
    private val udpClient: UdpClient,
    private val webSocketClient: WebSocketClient,
    private val discoveryClient: DiscoveryClient
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredServers = MutableStateFlow<List<DiscoveredServer>>(emptyList())
    val discoveredServers: StateFlow<List<DiscoveredServer>> = _discoveredServers.asStateFlow()

    private var activeTransport: TransportType? = null

    /**
     * Initiates UDP connection to PC server over WiFi.
     */
    fun connectUdp(host: String, port: Int) {
        scope.launch {
            _connectionState.value = ConnectionState.Connecting(TransportType.UDP, host)
            try {
                webSocketClient.close()
                udpClient.connect(host, port)
                activeTransport = TransportType.UDP
                _connectionState.value = ConnectionState.Connected(TransportType.UDP, host, port)
            } catch (e: Exception) {
                activeTransport = null
                _connectionState.value = ConnectionState.Failed("UDP connection error: ${e.message}")
            }
        }
    }

    /**
     * Initiates WebSocket connection to PC server over USB ADB loopback or WiFi.
     */
    fun connectWebSocket(url: String) {
        scope.launch {
            _connectionState.value = ConnectionState.Connecting(TransportType.WEBSOCKET, url)
            try {
                udpClient.close()
                val success = webSocketClient.connect(url)
                if (success) {
                    activeTransport = TransportType.WEBSOCKET
                    _connectionState.value = ConnectionState.Connected(TransportType.WEBSOCKET, url, 45679)
                } else {
                    activeTransport = null
                    _connectionState.value = ConnectionState.Failed("WebSocket connection failed to $url")
                }
            } catch (e: Exception) {
                activeTransport = null
                _connectionState.value = ConnectionState.Failed("WebSocket error: ${e.message}")
            }
        }
    }

    /**
     * Triggers UDP subnet broadcast discovery scan.
     */
    fun startDiscovery(timeoutMs: Int = DiscoveryClient.DEFAULT_DISCOVERY_TIMEOUT_MS) {
        scope.launch {
            _connectionState.value = ConnectionState.Discovering
            val servers = discoveryClient.scanForServers(timeoutMs)
            _discoveredServers.value = servers
            if (_connectionState.value is ConnectionState.Discovering) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    /**
     * Transmits a serialized packet over the active connection transport.
     */
    suspend fun sendPacket(bytes: ByteArray): Boolean {
        return when (activeTransport) {
            TransportType.UDP -> udpClient.send(bytes)
            TransportType.WEBSOCKET -> webSocketClient.send(bytes)
            null -> false
        }
    }

    /**
     * Disconnects active connection sessions.
     */
    fun disconnect() {
        scope.launch {
            udpClient.close()
            webSocketClient.close()
            activeTransport = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
