package dev.steeringphone.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-performance non-blocking UDP socket client transmitting binary DrivePackets over WiFi.
 */
@Singleton
class UdpClient @Inject constructor() {

    private var socket: DatagramSocket? = null
    private var targetInetAddress: InetAddress? = null
    private var targetPort: Int = 0

    val isConnected: Boolean
        get() = socket != null && !socket!!.isClosed && targetInetAddress != null

    /**
     * Initializes the UDP socket target address and port.
     */
    suspend fun connect(host: String, port: Int) = withContext(Dispatchers.IO) {
        close()
        targetInetAddress = InetAddress.getByName(host)
        targetPort = port
        socket = DatagramSocket()
    }

    /**
     * Transmits a binary packet over UDP.
     *
     * @param bytes Serialized byte array (e.g. 43-byte DrivePacket).
     */
    suspend fun send(bytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        val currentSocket = socket ?: return@withContext false
        val currentAddress = targetInetAddress ?: return@withContext false
        if (currentSocket.isClosed) return@withContext false

        try {
            val packet = DatagramPacket(bytes, bytes.size, currentAddress, targetPort)
            currentSocket.send(packet)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Closes the UDP socket.
     */
    fun close() {
        try {
            socket?.close()
        } catch (_: Exception) {}
        socket = null
        targetInetAddress = null
        targetPort = 0
    }
}
