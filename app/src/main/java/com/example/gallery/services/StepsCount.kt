package com.example.gallery.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

//class StepsCount(
//    context: Context,
//    workerParameters: WorkerParameters
//) : Worker(context, workerParameters), SensorEventListener {
//
//    private var initialStepCount = -1
//    private var steps = 0
//    private var distance: Double = 0.0
//    private var date: String = ""
//
//    override fun doWork(): Result {
//        setUpSensor()
//        return Result.success()
//    }
//
//    @SuppressLint("SimpleDateFormat")
//    override fun onSensorChanged(event: SensorEvent?) {
//        val sensorStepCount = event?.values?.firstOrNull()
//
//        sensorStepCount?.let {
//
//            val originalSteps = sensorStepCount.toInt()
//            println("original steps: $originalSteps")
//
//            if (initialStepCount < 0) {
//                initialStepCount = sensorStepCount.toInt()
//            }
//            steps = sensorStepCount.toInt() - initialStepCount
//        }
//
//
//        sensorStepCount.let {
////            val time = System.currentTimeMillis()
////            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
////            //date = simpleDateFormat.format(time)
//            distance = getDistance(steps.toDouble())
//
//
//            // Data 2: The number of nanosecond passed since the time of last boot
//            val lastDeviceBootTimeInMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()
//            val sensorEventTimeInNanos = event?.timestamp // The number of nanosecond passed since the time of last boot
//            val sensorEventTimeInMillis = sensorEventTimeInNanos?.div(1000_000)
//
//            val actualSensorEventTimeInMillis = lastDeviceBootTimeInMillis + sensorEventTimeInMillis!!
//            date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(actualSensorEventTimeInMillis)
//
//            sendBroadcast(applicationContext, steps, distance, date)
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        println("on accuracy changed: Sensor: $sensor, accuracy $accuracy")
//    }
//
//    private fun setUpSensor(){
//        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//
//        if (sensor == null){
//            println("No sensor found")
//            return
//        }
//
//        sensor.let {
//            sensorManager.registerListener(this,it, SensorManager.SENSOR_DELAY_FASTEST)
//        }
//    }
//
//    private fun calculateDistance(steps: Double): Double {
//        return steps * 78 / 100000
//    }
//
//    private fun getDistance(steps: Double): Double {
//        val height = 1.7
//        val stepLength = height * 0.45
//        return steps * stepLength
//    }
//
//    private fun sendBroadcast(context: Context, steps: Int, distance: Double, date: String){
//
//        val intent = Intent("com.example.steps")
//        println("at Send: steps $steps, distance $distance, date $date")
//        intent.putExtra("steps", steps)
//        intent.putExtra("distance", distance)
//        intent.putExtra("date", date)
//        GlobalScope.launch {
//            context.sendBroadcast(intent)
//        }
//        //context.sendBroadcast(intent)
//    }
//}