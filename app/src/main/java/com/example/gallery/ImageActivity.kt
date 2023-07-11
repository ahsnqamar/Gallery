package com.example.gallery

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gallery.databinding.ActivityImageBinding
import com.example.gallery.services.StepAcc
import com.example.gallery.services.StepCounter
import com.example.gallery.viewmodals.StepCounterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.prefs.Preferences


class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var viewModel: StepCounterViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private var goal = 100
    private var notificationSendCount = 0
    private lateinit var  decimalFormat : DecimalFormat

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("steps_counter", Context.MODE_PRIVATE)

        decimalFormat = DecimalFormat("#.##")

        getPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)

        viewModel = ViewModelProvider(this)[StepCounterViewModel::class.java]

        viewModel.stepCountLiveData.observe(this) { stepCount ->
            println("stepCount: $stepCount")
            binding.steps.text = stepCount.toString()
        }
        viewModel.distanceLiveData.observe(this) { distance ->
            binding.distance.text = distance.toString()
        }
        viewModel.dateLiveData.observe(this) { date ->
            binding.textView5.text = date
        }
        viewModel.caloriesBurnedLiveData.observe(this) { calories ->
            binding.textView6.text = calories.toString()
        }
        viewModel.totalMoveMinutesLiveData.observe(this) { moveMinutes ->
            binding.textView7.text = "$moveMinutes minutes"
        }


        binding.progressBar.max = goal



        CoroutineScope(Dispatchers.IO).launch {

            launch {
                val intent = Intent(this@ImageActivity, StepCounter::class.java)
                startService(intent)
            }
            val intentFilter = IntentFilter("com.example.STEPS_COUNT")
            LocalBroadcastManager.getInstance(this@ImageActivity).registerReceiver(broadcastReceiver, intentFilter)
        }
//        val request = OneTimeWorkRequestBuilder<StepsCount>().build()
//        WorkManager.getInstance(this).enqueue(request)

    }

    override fun onResume() {
        super.onResume()
        viewModel = ViewModelProvider(this)[StepCounterViewModel::class.java]


        val s = sharedPreferences.getInt("steps", 0)
        val d = sharedPreferences.getFloat("distance", 0.0f)
        val da = sharedPreferences.getString("date", "")
        val c = sharedPreferences.getFloat("calories", 0.0f)
        val m = sharedPreferences.getInt("move_minutes", 0)

        println("sharedPreferences: $s, $d, $da, $c")

        viewModel.updateStepCount(s)
        viewModel.updateDistance(d.toDouble())
        viewModel.updateDate(da!!)
        viewModel.updateCaloriesBurned(c.toDouble())
        viewModel.updateTotalMoveMinutes(m)

        binding.progressBar.progress = s

    }

    private fun getPermission(permission: String){
        if (ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_DENIED){
            // request for permission
            ActivityCompat.requestPermissions(this, arrayOf(permission),100)
        }else{
            //showToast("permission already granted")
            println("permission already granted")
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val steps = intent?.getIntExtra("steps", 0)
            val distance = intent?.getDoubleExtra("distance", 0.0)
            val date = intent?.getStringExtra("date")
            val caloriesBurned = intent?.getDoubleExtra("calories", 0.0)
            val moveMinutes = intent?.getIntExtra("moveMinutes", 0)
            println("at receiver: $steps, $distance, $date, $caloriesBurned, $moveMinutes")
            //val distanceInKm = distance?.div(1000)

            viewModel.updateStepCount(steps!!)
            viewModel.updateDistance(decimalFormat.format(distance!!).toDouble())
            viewModel.updateDate(date!!)
            viewModel.updateCaloriesBurned(caloriesBurned!!)
            viewModel.updateTotalMoveMinutes(moveMinutes!!)

            binding.progressBar.progress = steps

            calculatePercentage()

            if (steps >= goal && notificationSendCount == 0){
                createNotification("Goal Achieved", "You have achieved your goal of $goal steps")
                notificationSendCount++
            }


        }
    }

    private fun calculatePercentage(){
        val steps = viewModel.stepCountLiveData.value
        val percentage = (steps!! * 100) / goal
        binding.percentage.text = "$percentage%"
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    fun createNotification(title: String = "Notification Title", description: String = "Notification", smallIcon:Int = R.drawable.ic_launcher_background) {

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("101", "channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "101")
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(smallIcon)

        notificationManager.notify(1, notificationBuilder.build())
        println("Notified")

    }


}