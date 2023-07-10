package com.example.gallery

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.prefs.Preferences


class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var viewModel: StepCounterViewModel
    private lateinit var sharedPreferences: SharedPreferences



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        sharedPreferences = getSharedPreferences("steps_counter", Context.MODE_PRIVATE)
        val s = sharedPreferences.getInt("steps", 0)
        val d = sharedPreferences.getFloat("distance", 0.0f)
        val da = sharedPreferences.getString("date", "")
        val c = sharedPreferences.getFloat("calories", 0.0f)

        println("sharedPreferences: $s, $d, $da, $c")

        viewModel.updateStepCount(s)
        viewModel.updateDistance(d.toDouble())
        viewModel.updateDate(da!!)
        viewModel.updateCaloriesBurned(c.toDouble())


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
            println("at receiver: $steps, $distance, $date, $caloriesBurned")
            val distanceInMeters = "%.2f".format(distance)







            viewModel.updateStepCount(steps!!)
            viewModel.updateDistance(distance!!)
            viewModel.updateDate(date!!)
            viewModel.updateCaloriesBurned(caloriesBurned!!)

//            viewModel.updateStepCount(s )
//            viewModel.updateDistance(d.toDouble() )
//            viewModel.updateDate(da!!)
//            viewModel.updateCaloriesBurned(c.toDouble() )

        }
    }




    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

}