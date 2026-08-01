package dev.steeringphone.sensor

/**
 * Single-pole low-pass exponential smoothing filter for steering signal noise attenuation.
 *
 * Formula:
 * y[n] = y[n-1] + alpha * (x[n] - y[n-1])
 *
 * @property alpha Smoothing factor in range [0.0, 1.0]. Lower values give stronger smoothing.
 */
class LowPassFilter(
    var alpha: Float = DEFAULT_ALPHA
) {
    var filteredValue: Float = 0f
        private set

    private var isInitialized: Boolean = false

    /**
     * Filters an input value through the low-pass exponential smoothing function.
     *
     * @param input Raw incoming value.
     * @return Filtered output value.
     */
    fun filter(input: Float): Float {
        if (!isInitialized) {
            filteredValue = input
            isInitialized = true
            return filteredValue
        }

        filteredValue += alpha * (input - filteredValue)
        return filteredValue
    }

    /**
     * Resets the filter to an initial value.
     *
     * @param initialValue Value to prime the filter with (default 0.0f).
     */
    fun reset(initialValue: Float = 0f) {
        filteredValue = initialValue
        isInitialized = false
    }

    companion object {
        const val DEFAULT_ALPHA: Float = 0.15f
    }
}
