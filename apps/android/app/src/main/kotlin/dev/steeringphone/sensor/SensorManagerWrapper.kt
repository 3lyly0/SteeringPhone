package dev.steeringphone.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2

/**
 * Manages high-rate (120 Hz) Android sensor callbacks on a dedicated HandlerThread and processes
 * raw accelerometer/gyroscope readings through ComplementaryFilter, LowPassFilter, and SteeringCalculator.
 */
@Singleton
class SensorManagerWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var sensorThread: HandlerThread? = null
    private var sensorHandler: Handler? = null

    private val complementaryFilter = ComplementaryFilter()
    private val lowPassFilter = LowPassFilter()
    private val steeringCalculator = SteeringCalculator()

    private val _sensorDataFlow = MutableStateFlow(SensorData())
    val sensorDataFlow: StateFlow<SensorData> = _sensorDataFlow.asStateFlow()

    @Volatile
    private var isListening: Boolean = false

    private var lastTimestampNs: Long = 0L
    private var centerOffsetDegrees: Float = 0f

    private var currentAccelX = 0f
    private var currentAccelY = 0f
    private var currentAccelZ = 0f
    private var currentGyroX = 0f
    private var currentGyroY = 0f
    private var currentGyroZ = 0f

    /**
     * Starts listening to sensor events at fastest rate on a dedicated HandlerThread.
     */
    fun start() {
        if (isListening || sensorManager == null) return

        sensorThread = HandlerThread("SteeringPhoneSensorThread", Process.THREAD_PRIORITY_URGENT_DISPLAY).apply {
            start()
            sensorHandler = Handler(looper)
        }

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, sensorHandler)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, sensorHandler)
        }

        isListening = true
    }

    /**
     * Stops sensor listening and terminates the handler thread.
     */
    fun stop() {
        if (!isListening || sensorManager == null) return

        sensorManager.unregisterListener(this)
        sensorThread?.quitSafely()
        sensorThread = null
        sensorHandler = null
        isListening = false
        lastTimestampNs = 0L
    }

    /**
     * Calibrates current phone angle as the new center zero angle.
     */
    fun calibrateCenter() {
        val currentAngle = _sensorDataFlow.value.fusedAngleDegrees
        centerOffsetDegrees = currentAngle
        complementaryFilter.reset(0f)
        lowPassFilter.reset(0f)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                currentAccelX = event.values[0]
                currentAccelY = event.values[1]
                currentAccelZ = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                currentGyroX = event.values[0]
                currentGyroY = event.values[1]
                currentGyroZ = event.values[2]
            }
            else -> return
        }

        val nowNs = event.timestamp
        val dtSeconds = if (lastTimestampNs > 0L) (nowNs - lastTimestampNs) / 1_000_000_000f else 0.008333f // fallback ~120 Hz
        lastTimestampNs = nowNs

        // Calculate raw accelerometer tilt angle
        val rawAngle = Math.toDegrees(atan2(currentAccelY.toDouble(), currentAccelX.toDouble())).toFloat() - centerOffsetDegrees

        // Fuse accelerometer and gyroscope data
        val fusedAngle = complementaryFilter.update(rawAngle, currentGyroZ, dtSeconds)

        // Apply low pass filter for micro-jitter attenuation
        val smoothedAngle = lowPassFilter.filter(fusedAngle)

        // Calculate final normalized steering value
        val steeringAngle = steeringCalculator.calculate(smoothedAngle)

        _sensorDataFlow.value = SensorData(
            accelX = currentAccelX,
            accelY = currentAccelY,
            accelZ = currentAccelZ,
            gyroX = currentGyroX,
            gyroY = currentGyroY,
            gyroZ = currentGyroZ,
            timestampNs = nowNs,
            rawAngleDegrees = rawAngle,
            fusedAngleDegrees = smoothedAngle,
            steeringAngle = steeringAngle
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
