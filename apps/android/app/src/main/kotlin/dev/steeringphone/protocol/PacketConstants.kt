package dev.steeringphone.protocol

object PacketConstants {
    const val MAGIC: Byte = 0xD5.toByte()
    const val VERSION: Byte = 0x01
    const val PACKET_SIZE: Int = 43
    const val DISCOVERY_PORT: Int = 45678
    const val WEBSOCKET_PORT: Int = 45679
    const val UDP_DATA_PORT: Int = 45680
    const val HEARTBEAT_INTERVAL_MS: Long = 1000L
    const val CONNECTION_TIMEOUT_MS: Long = 3000L
}
