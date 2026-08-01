package dev.steeringphone.sensor

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SteeringCalculatorTest {

    private lateinit var calculator: SteeringCalculator

    @Before
    fun setUp() {
        calculator = SteeringCalculator(
            maxSteeringAngleDegrees = 90f,
            deadzone = 0.05f,
            sensitivity = 1.0f,
            exponentialCurve = 1.0f
        )
    }

    @Test
    fun testCenterIsZero() {
        val output = calculator.calculate(0f)
        assertEquals(0f, output, 0.001f)
    }

    @Test
    fun testDeadzoneSuppression() {
        // Deadzone is 0.05 * 90° = 4.5°
        // Any angle <= 4.5° should output 0.0
        assertEquals(0f, calculator.calculate(2f), 0.001f)
        assertEquals(0f, calculator.calculate(-4f), 0.001f)
        assertEquals(0f, calculator.calculate(4.5f), 0.001f)
    }

    @Test
    fun testFullLockClamping() {
        // Max angle is 90°
        assertEquals(1.0f, calculator.calculate(90f), 0.001f)
        assertEquals(-1.0f, calculator.calculate(-90f), 0.001f)
        assertEquals(1.0f, calculator.calculate(120f), 0.001f)
        assertEquals(-1.0f, calculator.calculate(-150f), 0.001f)
    }

    @Test
    fun testExponentialCurve() {
        // Set curve = 2.0 (quadratic), no deadzone, max = 90
        calculator.deadzone = 0f
        calculator.exponentialCurve = 2.0f

        // At 45° (half angle, normalized 0.5): 0.5^2 = 0.25
        val output = calculator.calculate(45f)
        assertEquals(0.25f, output, 0.001f)

        // Negative 45°: - (0.5^2) = -0.25
        val negOutput = calculator.calculate(-45f)
        assertEquals(-0.25f, negOutput, 0.001f)
    }

    @Test
    fun testSensitivityScaling() {
        calculator.deadzone = 0f
        calculator.sensitivity = 1.5f

        // At 30° (normalized 30/90 = 0.3333): 0.3333 * 1.5 = 0.5
        val output = calculator.calculate(30f)
        assertEquals(0.5f, output, 0.001f)

        // At 75° (normalized 75/90 = 0.8333): 0.8333 * 1.5 = 1.25 -> clamped to 1.0
        val clampedOutput = calculator.calculate(75f)
        assertEquals(1.0f, clampedOutput, 0.001f)
    }
}
