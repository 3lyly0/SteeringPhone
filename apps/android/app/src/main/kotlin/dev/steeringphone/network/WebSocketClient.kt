package dev.steeringphone.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ktor WebSocket client for reliable USB ADB socket transport.
 */
@Singleton
class WebSocketClient @Inject constructor() {

    private var httpClient: HttpClient? = null
    private var session: DefaultClientWebSocketSession? = null

    val isConnected: Boolean
        get() = session != null

    /**
     * Connects to a WebSocket endpoint URL.
     */
    suspend fun connect(url: String): Boolean = withContext(Dispatchers.IO) {
        close()
        try {
            val client = HttpClient(CIO) {
                install(WebSockets)
            }
            httpClient = client
            session = client.webSocketSession(url)
            true
        } catch (e: Exception) {
            close()
            false
        }
    }

    /**
     * Transmits a binary DrivePacket frame over the WebSocket session.
     */
    suspend fun send(bytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        val currentSession = session ?: return@withContext false
        try {
            currentSession.send(Frame.Binary(fin = true, data = bytes))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Closes the active WebSocket session and HTTP client.
     */
    suspend fun close() = withContext(Dispatchers.IO) {
        try {
            session?.close()
        } catch (_: Exception) {}
        try {
            httpClient?.close()
        } catch (_: Exception) {}
        session = null
        httpClient = null
    }
}
