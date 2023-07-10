package com.example.gallery.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gallery.MainActivity2
import com.example.gallery.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar

class StepCounter : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var initialStepCount :Int ?= null
    private var steps = 0
    private var distance: Double = 0.0
    private var date: String = ""
    private var caloriesBurned: Double = 0.0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        println("1")
        super.onCreate()

        sensorManager = applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        } else{
            println("Sensor not found")
        }

        sendNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("2")


        return START_STICKY
    }

    override fun onDestroy() {
        println("3")
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }



    override fun onBind(intent: Intent?): IBinder? {
        println("4")
        return null
    }

    @SuppressLint("SimpleDateFormat")
    override fun onSensorChanged(event: SensorEvent?) {
        println("5")

        GlobalScope.launch {
            withContext(Dispatchers.IO){
                val sensorStepCount = event?.values?.firstOrNull()?.toInt()

                sensorStepCount.let {
                    val originalSteps = it
                    println("original steps: $originalSteps")

                    if (initialStepCount == null) {
                        initialStepCount = it
                        println("initial steps: $initialStepCount")
                    }

                    if (it !! >= initialStepCount!!){
                        steps = it - initialStepCount!!
                    } else {
                        initialStepCount = it
                        steps = 0
                    }
                    println("steps: $steps")
                    distance = getDistance(steps.toDouble())

                    // Data 2: The number of nanosecond passed since the time of last boot
                    val value = async {
                        val lastDeviceBootTimeInMillis =
                            System.currentTimeMillis() - SystemClock.elapsedRealtime()
                        val sensorEventTimeInNanos =
                            event?.timestamp // The number of nanosecond passed since the time of last boot
                        val sensorEventTimeInMillis = sensorEventTimeInNanos?.div(1000_000)

                        val actualSensorEventTimeInMillis =
                            lastDeviceBootTimeInMillis + sensorEventTimeInMillis!!
                        actualSensorEventTimeInMillis
                    }

                    date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(value.await())

                    val calBurned = async {
                        calculateCaloriesBurned()
                    }
                    caloriesBurned = calBurned.await()

                    sharedPreferences = getSharedPreferences("steps_counter", Context.MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putInt("steps", steps)
                        putFloat("distance", distance.toFloat())
                        putString("date", date)
                        putFloat("calories", caloriesBurned.toFloat())
                        apply()
                    }

                    println("steps coroutines $steps, distance $distance, date $date, calories $caloriesBurned")
                    withContext(Dispatchers.Main){
                        sendBroadcast(applicationContext, steps, distance, date, caloriesBurned)
                    }
                }
            }

        }


    }

    private fun calculateMidNightTime(){
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val midnight = calendar.timeInMillis
        println("midnight $midnight")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("6")
        println("on accuracy changed: Sensor: $sensor, accuracy $accuracy")
    }

    private fun getDistance(steps: Double): Double {
        val height = 1.7
        val stepLength = height * 0.45
        return steps * stepLength
    }

    private fun calculateCaloriesBurned(): Double {
        val weight = 70
        val caloriesPerMile = 0.57 * weight
        val caloriesPerKm = caloriesPerMile / 1.6
        return caloriesPerKm * distance
    }

    private fun sendBroadcast(context: Context, steps: Int, distance: Double, date: String, caloriesBurned: Double) {

        val intent = Intent("com.example.STEPS_COUNT")
        println("at Send: steps $steps, distance $distance, date $date")
        intent.putExtra("steps", steps)
        intent.putExtra("distance", distance)
        intent.putExtra("date", date)
        intent.putExtra("calories", caloriesBurned)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        //context.sendBroadcast(intent)
    }

    private fun sendNotification(){
        // Step 1: Define the Notification Channel (if needed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Step 2: Create the Notification
        val channelId = "my_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fitness App")
            .setContentText("Activity is being tracked...")
            .setSmallIcon(R.drawable.play)

        // Step 3: Create a Pending Intent
        val notificationIntent = Intent(this, MainActivity2::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.setContentIntent(pendingIntent)

        // Step 4: Start the Service as a Foreground Service
        val notification = notificationBuilder.build()
        startForeground(101, notification)

    }
}