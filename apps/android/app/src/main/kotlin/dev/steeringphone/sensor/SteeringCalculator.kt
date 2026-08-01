package dev.steeringphone.sensor

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

/**
 * Calculates normalized steering value [-1.0, +1.0] from physical phone rotation angle in degrees.
 *
 * Applies deadzone, exponential response curve, sensitivity scaling, and range clamping.
 *
 * @property maxSteeringAngleDegrees Maximum tilt angle in degrees corresponding to full wheel lock [-1.0, +1.0].
 * @property deadzone Center deadband threshold in ratio [0.0, 0.5].
 * @property sensitivity Multiplier for steering response (> 0.0).
 * @property exponentialCurve Non-linear exponent curve for progressive steering sensitivity around center (>= 1.0).
 */
class SteeringCalculator(
    var maxSteeringAngleDegrees: Float = DEFAULT_MAX_STEERING_ANGLE,
    var deadzone: Float = DEFAULT_DEADZONE,
    var sensitivity: Float = DEFAULT_SENSITIVITY,
    var exponentialCurve: Float = DEFAULT_EXPONENTIAL_CURVE
) {

    /**
     * Calculates normalized steering output value in range [-1.0, +1.0] from rotation angle in degrees.
     *
     * @param angleDegrees Phone rotation angle in degrees relative to center.
     * @return Normalized steering value between -1.0 (full left) and +1.0 (full right).
     */
    fun calculate(angleDegrees: Float): Float {
        if (maxSteeringAngleDegrees <= 0f) return 0f

        // Step 1: Normalize angle to [-1.0, +1.0] and clamp base input
        val rawNormalized = (angleDegrees / maxSteeringAngleDegrees).coerceIn(-1f, 1f)

        // Step 2: Apply deadzone
        val clampedDeadzone = deadzone.coerceIn(0f, 0.9f)
        val absVal = abs(rawNormalized)
        if (absVal <= clampedDeadzone) {
            return 0f
        }

        val deadzoneAdjusted = sign(rawNormalized) * ((absVal - clampedDeadzone) / (1f - clampedDeadzone))

        // Step 3: Apply exponential curve
        val exp = exponentialCurve.coerceAtLeast(1f)
        val curved = sign(deadzoneAdjusted) * abs(deadzoneAdjusted).toDouble().pow(exp.toDouble()).toFloat()

        // Step 4: Apply sensitivity scaling and clamp output
        val scaled = curved * sensitivity
        return scaled.coerceIn(-1f, 1f)
    }

    companion object {
        const val DEFAULT_MAX_STEERING_ANGLE: Float = 90f
        const val DEFAULT_DEADZONE: Float = 0.05f
        const val DEFAULT_SENSITIVITY: Float = 1.0f
        const val DEFAULT_EXPONENTIAL_CURVE: Float = 1.0f
    }
}
