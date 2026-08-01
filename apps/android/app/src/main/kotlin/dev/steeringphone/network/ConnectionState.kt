package dev.steeringphone.network

enum class TransportType {
    UDP,
    WEBSOCKET
}

/**
 * Represents the current network connection state of the SteeringPhone Android app.
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Discovering : ConnectionState()
    data class Connecting(val transport: TransportType, val address: String) : ConnectionState()
    data class Connected(val transport: TransportType, val address: String, val port: Int) : ConnectionState()
    data class Reconnecting(val attempt: Int, val maxAttempts: Int) : ConnectionState()
    data class Failed(val reason: String) : ConnectionState()
}

/**
 * Data class representing a discovered PC server instance on the local network.
 */
data class DiscoveredServer(
    val hostname: String,
    val ipAddress: String,
    val port: Int,
    val transport: TransportType = TransportType.UDP
)
