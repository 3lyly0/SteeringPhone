package dev.steeringphone.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolSerializerTest {

    @Test
    fun testSerializationAndDeserializationRoundtrip() {
        val originalPacket = DrivePacket(
            sequenceNumber = 1234u,
            timestampUs = 1690000000000000L,
            steeringAngle = -0.75f,
            accelX = 0.12f,
            accelY = 9.81f,
            accelZ = -0.05f,
            gyroZ = 1.25f,
            buttonMask = (ButtonMask.THROTTLE_BTN.toInt() or ButtonMask.GEAR_UP.toInt()).toUShort(),
            throttle = 255u,
            brake = 0u,
            clutch = 128u,
            batteryPercentage = 85u,
            signalQuality = 99u,
            pingMs = 12u
        )

        val bytes = ProtocolSerializer.serialize(originalPacket)
        assertEquals(PacketConstants.PACKET_SIZE, bytes.size)

        val result = ProtocolSerializer.deserialize(bytes)
        assertTrue(result.isSuccess)

        val decoded = result.getOrThrow()
        assertEquals(originalPacket.magic, decoded.magic)
        assertEquals(originalPacket.version, decoded.version)
        assertEquals(originalPacket.sequenceNumber, decoded.sequenceNumber)
        assertEquals(originalPacket.timestampUs, decoded.timestampUs)
        assertEquals(originalPacket.steeringAngle, decoded.steeringAngle, 0.0001f)
        assertEquals(originalPacket.accelX, decoded.accelX, 0.0001f)
        assertEquals(originalPacket.accelY, decoded.accelY, 0.0001f)
        assertEquals(originalPacket.accelZ, decoded.accelZ, 0.0001f)
        assertEquals(originalPacket.gyroZ, decoded.gyroZ, 0.0001f)
        assertEquals(originalPacket.buttonMask, decoded.buttonMask)
        assertEquals(originalPacket.throttle, decoded.throttle)
        assertEquals(originalPacket.brake, decoded.brake)
        assertEquals(originalPacket.clutch, decoded.clutch)
        assertEquals(originalPacket.batteryPercentage, decoded.batteryPercentage)
        assertEquals(originalPacket.signalQuality, decoded.signalQuality)
        assertEquals(originalPacket.pingMs, decoded.pingMs)
    }

    @Test
    fun testRejectionOfInvalidMagicByte() {
        val originalPacket = DrivePacket(sequenceNumber = 1u, timestampUs = 0L, steeringAngle = 0f, accelX = 0f, accelY = 0f, accelZ = 0f, gyroZ = 0f, buttonMask = 0u, throttle = 0u, brake = 0u, clutch = 0u, batteryPercentage = 0u, signalQuality = 0u, pingMs = 0u)
        val bytes = ProtocolSerializer.serialize(originalPacket)
        bytes[0] = 0xAA.toByte() // Corrupt magic

        val result = ProtocolSerializer.deserialize(bytes)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid magic byte") == true)
    }

    @Test
    fun testRejectionOfCorruptedCrc() {
        val originalPacket = DrivePacket(sequenceNumber = 42u, timestampUs = 1000L, steeringAngle = 0.5f, accelX = 0f, accelY = 0f, accelZ = 0f, gyroZ = 0f, buttonMask = 0u, throttle = 100u, brake = 0u, clutch = 0u, batteryPercentage = 50u, signalQuality = 50u, pingMs = 5u)
        val bytes = ProtocolSerializer.serialize(originalPacket)
        bytes[12] = (bytes[12].toInt() xor 0xFF).toByte() // Corrupt floating point steering value

        val result = ProtocolSerializer.deserialize(bytes)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("CRC-16 mismatch") == true)
    }
}
