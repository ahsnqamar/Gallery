package com.example.gallery.services

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import kotlin.math.abs
import kotlin.math.sqrt

class StepAcc: Service(), SensorEventListener {

    private val threshold = 2.0
    private var prevY = 0.0

    // Gravity for accelerometer data
    private val gravity = FloatArray(3)
    // smoothed values
    private var smoothed = FloatArray(3)
    private var stepCount = 0

    private lateinit var sensorManager: SensorManager

    override fun onCreate() {
        super.onCreate()

        sensorManager = applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        } else{
            println("Sensor not found")
        }

    }

    private fun lowPassFilter(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + 1.0f * (input[i] - output[i])
        }
        return output
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Apply low-pass filter to the accelerometer data
        smoothed = lowPassFilter(event.values.clone(), smoothed)

        // Calculate the magnitude of the acceleration vector
        val magnitude = sqrt(
            (smoothed[0] * smoothed[0] +
                    smoothed[1] * smoothed[1] +
                    smoothed[2] * smoothed[2]).toDouble()
        )

        // Check if a step is detected based on the magnitude
        if (isStepDetected(magnitude)) {
            stepCount++
            println("Step detected: $stepCount")
            // Update UI or perform other necessary actions
        }
    }

    private fun isStepDetected(magnitude: Double): Boolean {
        // Compare the current magnitude with the previous magnitude and a threshold
        val deltaMagnitude = magnitude - prevY
        if (deltaMagnitude > threshold) {
            prevY = magnitude
            return true
        }
        return false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }
}