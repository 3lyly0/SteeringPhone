package dev.steeringphone.sensor

/**
 * Encapsulates raw sensor readings and calculated steering values.
 */
data class SensorData(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val timestampNs: Long = 0L,
    val rawAngleDegrees: Float = 0f,
    val fusedAngleDegrees: Float = 0f,
    val steeringAngle: Float = 0f
)
