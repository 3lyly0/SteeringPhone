package dev.steeringphone.protocol

data class DrivePacket(
    val magic: Byte = PacketConstants.MAGIC,
    val version: Byte = PacketConstants.VERSION,
    val sequenceNumber: UShort,
    val timestampUs: Long,
    val steeringAngle: Float,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val gyroZ: Float,
    val buttonMask: UShort,
    val throttle: UByte,
    val brake: UByte,
    val clutch: UByte,
    val batteryPercentage: UByte,
    val signalQuality: UByte,
    val pingMs: UShort,
    val crc16: UShort = 0u
)
