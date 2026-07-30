package dev.steeringphone.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

object ProtocolSerializer {

    fun calculateCrc16(data: ByteArray, length: Int): UShort {
        var crc = 0xFFFF
        for (i in 0 until length) {
            crc = crc xor ((data[i].toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if ((crc and 0x8000) != 0) {
                    (crc shl 1) xor 0x1021
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }
        return crc.toUShort()
    }

    fun serialize(packet: DrivePacket, buffer: ByteArray = ByteArray(PacketConstants.PACKET_SIZE)): ByteArray {
        require(buffer.size >= PacketConstants.PACKET_SIZE) { "Buffer size must be at least ${PacketConstants.PACKET_SIZE} bytes" }

        val bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
        bb.put(packet.magic)
        bb.put(packet.version)
        bb.putShort(packet.sequenceNumber.toShort())
        bb.putLong(packet.timestampUs)
        bb.putFloat(packet.steeringAngle)
        bb.putFloat(packet.accelX)
        bb.putFloat(packet.accelY)
        bb.putFloat(packet.accelZ)
        bb.putFloat(packet.gyroZ)
        bb.putShort(packet.buttonMask.toShort())
        bb.put(packet.throttle.toByte())
        bb.put(packet.brake.toByte())
        bb.put(packet.clutch.toByte())
        bb.put(packet.batteryPercentage.toByte())
        bb.put(packet.signalQuality.toByte())
        bb.putShort(packet.pingMs.toShort())

        val calculatedCrc = calculateCrc16(buffer, 41)
        bb.putShort(calculatedCrc.toShort())

        return buffer
    }

    fun deserialize(buffer: ByteArray): Result<DrivePacket> {
        if (buffer.size < PacketConstants.PACKET_SIZE) {
            return Result.failure(IllegalArgumentException("Buffer underflow: expected at least ${PacketConstants.PACKET_SIZE} bytes, got ${buffer.size}"))
        }

        val bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
        val magic = bb.get()
        if (magic != PacketConstants.MAGIC) {
            return Result.failure(IllegalArgumentException("Invalid magic byte: 0x${Integer.toHexString(magic.toInt() and 0xFF)}, expected 0xD5"))
        }

        val version = bb.get()
        if (version != PacketConstants.VERSION) {
            return Result.failure(IllegalArgumentException("Unsupported protocol version: $version, expected ${PacketConstants.VERSION}"))
        }

        val sequenceNumber = bb.short.toUShort()
        val timestampUs = bb.long
        val steeringAngle = bb.float
        val accelX = bb.float
        val accelY = bb.float
        val accelZ = bb.float
        val gyroZ = bb.float
        val buttonMask = bb.short.toUShort()
        val throttle = bb.get().toUByte()
        val brake = bb.get().toUByte()
        val clutch = bb.get().toUByte()
        val batteryPercentage = bb.get().toUByte()
        val signalQuality = bb.get().toUByte()
        val pingMs = bb.short.toUShort()
        val receivedCrc = bb.short.toUShort()

        val expectedCrc = calculateCrc16(buffer, 41)
        if (receivedCrc != expectedCrc) {
            return Result.failure(IllegalStateException("CRC-16 mismatch: received 0x${receivedCrc.toString(16)}, expected 0x${expectedCrc.toString(16)}"))
        }

        val packet = DrivePacket(
            magic = magic,
            version = version,
            sequenceNumber = sequenceNumber,
            timestampUs = timestampUs,
            steeringAngle = steeringAngle,
            accelX = accelX,
            accelY = accelY,
            accelZ = accelZ,
            gyroZ = gyroZ,
            buttonMask = buttonMask,
            throttle = throttle,
            brake = brake,
            clutch = clutch,
            batteryPercentage = batteryPercentage,
            signalQuality = signalQuality,
            pingMs = pingMs,
            crc16 = receivedCrc
        )

        return Result.success(packet)
    }
}
