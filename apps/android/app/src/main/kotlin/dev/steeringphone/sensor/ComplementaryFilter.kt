package dev.steeringphone.sensor

import kotlin.math.atan2

/**
 * Complementary filter algorithm fusing gyroscope angular velocity with accelerometer tilt vector.
 *
 * Formula:
 * Angle_fused = alpha * (Angle_prev + gyroZ_deg * dt) + (1 - alpha) * accelAngle_deg
 *
 * @property alpha Trust factor for gyroscope integration relative to accelerometer correction (default 0.98f).
 */
class ComplementaryFilter(
    var alpha: Float = DEFAULT_ALPHA
) {
    var fusedAngle: Float = 0f
        private set

    private var isInitialized: Boolean = false

    /**
     * Updates the fused angle estimation given accelerometer-derived angle and gyro angular velocity.
     *
     * @param accelAngleDegrees Tilt angle computed from accelerometer in degrees.
     * @param gyroZRadPerSec Gyroscope angular rate around steering axis in radians per second.
     * @param dtSeconds Time delta since last update in seconds.
     * @return The updated fused steering angle in degrees.
     */
    fun update(accelAngleDegrees: Float, gyroZRadPerSec: Float, dtSeconds: Float): Float {
        if (dtSeconds <= 0f) {
            return fusedAngle
        }

        val gyroZDegPerSec = Math.toDegrees(gyroZRadPerSec.toDouble()).toFloat()

        if (!isInitialized) {
            fusedAngle = accelAngleDegrees
            isInitialized = true
            return fusedAngle
        }

        val gyroIntegration = fusedAngle + gyroZDegPerSec * dtSeconds
        fusedAngle = alpha * gyroIntegration + (1f - alpha) * accelAngleDegrees
        return fusedAngle
    }

    /**
     * Updates fused angle directly from raw accelerometer X and Y forces.
     *
     * @param accelX Accelerometer X force in m/s^2.
     * @param accelY Accelerometer Y force in m/s^2.
     * @param gyroZRadPerSec Gyroscope Z rate in rad/s.
     * @param dtSeconds Time delta in seconds.
     * @return The updated fused steering angle in degrees.
     */
    fun updateRaw(accelX: Float, accelY: Float, gyroZRadPerSec: Float, dtSeconds: Float): Float {
        val accelAngle = Math.toDegrees(atan2(accelY.toDouble(), accelX.toDouble())).toFloat()
        return update(accelAngle, gyroZRadPerSec, dtSeconds)
    }

    /**
     * Resets the filter state to a specified initial angle in degrees.
     */
    fun reset(initialAngleDegrees: Float = 0f) {
        fusedAngle = initialAngleDegrees
        isInitialized = false
    }

    companion object {
        const val DEFAULT_ALPHA: Float = 0.98f
    }
}
