package com.example.gallery.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.example.gallery.MainActivity2
import com.example.gallery.R
import com.example.gallery.models.Steps
import com.example.gallery.receivers.TimeReceiver
import com.example.gallery.room.StepsDB
import com.example.gallery.viewmodals.StepCounterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class StepCounter : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var initialStepCount :Int ?= null
    private var steps: Int = 0
    private var distance: Double = 0.0
    private var date: String = ""
    private var caloriesBurned: Double = 0.0
    private lateinit var sharedPreferences: SharedPreferences

    //private var lastMovementTime: Long = 0
    private var moveStartTime = 0L
    private var totalMoveMinutes: Int = 0
    private val MOVE_MINUTES_THRESHOLD: Long = 1 * 60 * 1000
    private lateinit var stepViewModel: StepCounterViewModel

    override fun onCreate() {
        println("1")
        super.onCreate()

        sensorManager = applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sharedPreferences = getSharedPreferences("steps_counter", Context.MODE_PRIVATE)



        stepViewModel = StepCounterViewModel()

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        } else{
            println("Sensor not found")
        }

        val intentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(timeReceiver, intentFilter)

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
        unregisterReceiver(timeReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        println("4")
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

                    sharedPreferences.edit().apply {
                        putInt("steps", steps)
                        putFloat("distance", distance.toFloat())
                        putString("date", date)
                        putFloat("calories", caloriesBurned.toFloat())
                        putInt("moveMinutes", totalMoveMinutes)
                        apply()
                    }

                    val moveMinutes = async {
                        calculateMoveMinutes(steps)
                    }
                    moveMinutes.await()

                    println("steps coroutines $steps, distance $distance, date $date, calories $caloriesBurned, moveMinutes $totalMoveMinutes")

                    withContext(Dispatchers.Main){
                        sendBroadcast(applicationContext, steps, distance, date, caloriesBurned, totalMoveMinutes)
                    }
                }
            }

        }

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

    private fun sendBroadcast(context: Context, steps: Int, distance: Double, date: String, caloriesBurned: Double, moveMinutes: Int ) {

        val intent = Intent("com.example.STEPS_COUNT")
        println("at Send: steps $steps, distance $distance, date $date")
        intent.putExtra("steps", steps)
        intent.putExtra("distance", distance)
        intent.putExtra("date", date)
        intent.putExtra("calories", caloriesBurned)
        intent.putExtra("moveMinutes", moveMinutes)
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

    private fun calculateMoveMinutes(steps: Int) {
        if (steps > 0) {
            if (moveStartTime == 0L) {
                moveStartTime = System.currentTimeMillis()
            } else {
                if (moveStartTime != 0L) {

                    println("move start time: $moveStartTime")
                    val currentTime = System.currentTimeMillis()
                    val timeDifference = currentTime - moveStartTime
                    println("time difference: $timeDifference")
                    if (timeDifference >= MOVE_MINUTES_THRESHOLD) {
                        totalMoveMinutes += 1
                        moveStartTime = currentTime
                        println("move minutes: $totalMoveMinutes")
                    }
                    moveStartTime = 0L
                }
            }
        }
    }

    private fun insertDataIntoDB(){
        val database = StepsDB.getInstance(applicationContext)
        val stepsDao = database.stepDao()

        CoroutineScope(Dispatchers.IO).launch {
            populateDB(StepsDB.getInstance(applicationContext))
            val steps = stepsDao.getSteps()
            println("steps: $steps")
        }
    }

    private fun resetAllData(){
        sharedPreferences.edit().apply {
            putInt("steps", 0)
            putFloat("distance", 0f)
            putString("date", "")
            putFloat("calories", 0f)
            putInt("moveMinutes", 0)
            apply()
        }
        steps = 0
        distance = 0.0
        date = ""
        caloriesBurned = 0.0
        totalMoveMinutes = 0

        println("steps of view model${stepViewModel.stepCountLiveData.value}")

        stepViewModel.updateStepCount(steps)
        stepViewModel.updateDistance(distance)
        stepViewModel.updateDate(date)
        stepViewModel.updateCaloriesBurned(caloriesBurned)
        stepViewModel.updateTotalMoveMinutes(totalMoveMinutes)


        sendBroadcast(applicationContext, steps, distance, date, caloriesBurned, totalMoveMinutes)
    }

    private fun populateDB(db: StepsDB){
        val stepDao =  db.stepDao()
        stepDao.insert(Steps(steps, distance, date, caloriesBurned, totalMoveMinutes))
    }

    private var resetCounter = 0
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("time receiver")
            if (intent?.action == Intent.ACTION_TIME_TICK) {
                println("time tick")
                insertDataIntoDB()
                if (resetCounter < 2){
                    resetAllData()
                    resetCounter += 1
                }
            }
        }
    }

}
