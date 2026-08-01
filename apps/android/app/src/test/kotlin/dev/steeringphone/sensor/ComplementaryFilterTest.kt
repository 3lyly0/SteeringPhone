package dev.steeringphone.sensor

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ComplementaryFilterTest {

    private lateinit var filter: ComplementaryFilter

    @Before
    fun setUp() {
        filter = ComplementaryFilter(alpha = 0.98f)
    }

    @Test
    fun testInitialUpdateUsesAccelAngle() {
        val initialAngle = filter.update(accelAngleDegrees = 45f, gyroZRadPerSec = 0f, dtSeconds = 0.016f)
        assertEquals(45f, initialAngle, 0.001f)
    }

    @Test
    fun testPureGyroIntegrationWhenAlphaIsOne() {
        filter.alpha = 1.0f
        filter.update(accelAngleDegrees = 0f, gyroZRadPerSec = 0f, dtSeconds = 0.016f) // Initialized to 0°

        // Gyro rotation: 1.0 rad/s (~57.2958 deg/s) for 1 second in steps of 0.1s
        val gyroRadPerSec = 1.0f
        val dt = 0.1f
        var currentFused = 0f

        for (i in 1..10) {
            currentFused = filter.update(accelAngleDegrees = 0f, gyroZRadPerSec = gyroRadPerSec, dtSeconds = dt)
        }

        val expectedDegrees = Math.toDegrees(1.0).toFloat()
        assertEquals(expectedDegrees, currentFused, 0.01f)
    }

    @Test
    fun testPureAccelCorrectionWhenAlphaIsZero() {
        filter.alpha = 0.0f
        filter.update(accelAngleDegrees = 0f, gyroZRadPerSec = 0f, dtSeconds = 0.016f)

        // Accelerometer steps to 30 degrees, gyro says 0
        val updatedAngle = filter.update(accelAngleDegrees = 30f, gyroZRadPerSec = 0f, dtSeconds = 0.016f)
        assertEquals(30f, updatedAngle, 0.001f)
    }

    @Test
    fun testFilterConvergenceWithGyroAndAccel() {
        // Initialize at 0°
        filter.update(accelAngleDegrees = 0f, gyroZRadPerSec = 0f, dtSeconds = 0.016f)

        // Accelerometer tilts to 10°, gyro rate is 0
        var fused = 0f
        for (i in 1..200) {
            fused = filter.update(accelAngleDegrees = 10f, gyroZRadPerSec = 0f, dtSeconds = 0.016f)
        }

        // Fused angle should converge towards 10° over time
        assertEquals(10f, fused, 0.5f)
    }

    @Test
    fun testReset() {
        filter.update(accelAngleDegrees = 45f, gyroZRadPerSec = 0f, dtSeconds = 0.016f)
        filter.reset(15f)

        assertEquals(15f, filter.fusedAngle, 0.001f)
    }
}
