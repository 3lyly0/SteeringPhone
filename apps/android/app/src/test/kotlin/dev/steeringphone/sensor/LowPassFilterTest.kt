package dev.steeringphone.sensor

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LowPassFilterTest {

    private lateinit var filter: LowPassFilter

    @Before
    fun setUp() {
        filter = LowPassFilter(alpha = 0.15f)
    }

    @Test
    fun testInitialInputPassesThrough() {
        val result = filter.filter(100f)
        assertEquals(100f, result, 0.001f)
    }

    @Test
    fun testStepInputSmoothing() {
        // Initialize at 0
        filter.filter(0f)

        // Step input to 10.0
        val step1 = filter.filter(10f) // 0 + 0.15 * (10 - 0) = 1.5
        assertEquals(1.5f, step1, 0.001f)

        val step2 = filter.filter(10f) // 1.5 + 0.15 * (10 - 1.5) = 2.775
        assertEquals(2.775f, step2, 0.001f)
    }

    @Test
    fun testNoSmoothingWhenAlphaIsOne() {
        filter.alpha = 1.0f
        filter.filter(0f)

        val result = filter.filter(50f)
        assertEquals(50f, result, 0.001f)
    }

    @Test
    fun testReset() {
        filter.filter(100f)
        filter.reset(25f)
        assertEquals(25f, filter.filteredValue, 0.001f)
    }
}
