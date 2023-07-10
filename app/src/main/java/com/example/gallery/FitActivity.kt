package com.example.gallery

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gallery.databinding.ActivityFitBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.tasks.Tasks
import kotlin.math.roundToInt

class FitActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var totalSteps = 0
    private var totalCalories = 0f
    private var totalDistance = 0f
    private lateinit var binding: ActivityFitBinding
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        totalSteps = sharedPreferences.getInt("steps", 0)
        totalDistance = sharedPreferences.getFloat("distance", 0f)
        totalCalories = sharedPreferences.getFloat("calories", 0f)

        getPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
        getPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Fitness.SCOPE_ACTIVITY_READ)
            .requestScopes(Fitness.SCOPE_LOCATION_READ)
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        signIn()

        if (hasFitPermissions()) {
            println("has fit permissions")
        } else {
            println("does not have fit permissions")
        }

        //getStepCountForToday()


    }

    private fun signIn() {
        val signIntent = googleSignInClient.signInIntent
        startActivityForResult(signIntent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            println("account: $account")

            getStepCountForToday()

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hasFitPermissions(): Boolean {
        val fitnessPermissions = arrayOf(
            android.Manifest.permission.BODY_SENSORS,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        return ActivityCompat.checkSelfPermission(
            this,
            fitnessPermissions[0]
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun getPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            // request for permission
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        } else {
            //showToast("permission already granted")
            println("permission already granted")
        }
    }


    private val stepCountListener =
        OnDataPointListener { dataPoint ->
            for (field in dataPoint.dataType.fields) {
                val stepCount = dataPoint.getValue(field).asInt()
                totalSteps += stepCount
                println("steps count: $totalSteps")
                binding.tvStepCount.text = totalSteps.toString()
                println("real time steps: $stepCount")

//                getDistance(stepCount)
//                getCalories(stepCount)

                getDistanceAndCal()

            }
        }

//    private fun getDistance(stepCount: Int){
//
//        // get distance in meters
//        val distance = stepCount * 0.762
//        totalDistance += distance.toFloat()
//        binding.tvDistance.text = totalDistance.toString()
//    }

//    private fun getCalories(stepCount: Int){
//        val calories = stepCount * 0.04
//        totalCalories += calories.toFloat()
//        binding.tvCalories.text = totalCalories.toString()
//    }

//    private fun retrieveDistance() {
//        val startTime = Calendar.getInstance()
//        startTime.set(Calendar.YEAR, 2023)
//        startTime.set(Calendar.MONTH, Calendar.JULY)
//        startTime.set(Calendar.DAY_OF_MONTH, 1)
//        startTime.set(Calendar.HOUR_OF_DAY, 0)
//        startTime.set(Calendar.MINUTE, 0)
//        startTime.set(Calendar.SECOND, 0)
//
//        val endTime = Calendar.getInstance()
//        endTime.set(Calendar.YEAR, 2023)
//        endTime.set(Calendar.MONTH, Calendar.JULY)
//        endTime.set(Calendar.DAY_OF_MONTH, 10)
//        endTime.set(Calendar.HOUR_OF_DAY, 23)
//        endTime.set(Calendar.MINUTE, 59)
//        endTime.set(Calendar.SECOND, 59)
//
//
//        val distanceRequest = DataReadRequest.Builder()
//            .setTimeRange(startTime.timeInMillis, endTime.timeInMillis, TimeUnit.MILLISECONDS)
//            .read(DataType.TYPE_DISTANCE_DELTA)
//            .build()
//
//        println("distance request: $distanceRequest")
//
//        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
//            .readData(distanceRequest)
//            .addOnSuccessListener { response ->
//                val distanceDataSet = response.getDataSet(DataType.TYPE_DISTANCE_DELTA)
//                val distances = distanceDataSet.dataPoints
//                println("distances: $distances")
//                for (distance in distances) {
//                    for (field in distance.dataType.fields) {
//                        val distanceInMeters = distance.getValue(field).asFloat()
//                        totalDistance += distanceInMeters
//                        binding.tvDistance.text = distanceInMeters.toString()
//                        println("distance in meters: $distanceInMeters")
//                    }
//                }
//            }
//
//    }
//
//
//    private fun retrieveCalories() {
//
//        val startTime = Calendar.getInstance()
//        startTime.set(Calendar.YEAR, 2023)
//        startTime.set(Calendar.MONTH, Calendar.JULY)
//        startTime.set(Calendar.DAY_OF_MONTH, 1)
//        startTime.set(Calendar.HOUR_OF_DAY, 0)
//        startTime.set(Calendar.MINUTE, 0)
//        startTime.set(Calendar.SECOND, 0)
//
//        val endTime = Calendar.getInstance()
//        endTime.set(Calendar.YEAR, 2023)
//        endTime.set(Calendar.MONTH, Calendar.JULY)
//        endTime.set(Calendar.DAY_OF_MONTH, 10)
//        endTime.set(Calendar.HOUR_OF_DAY, 23)
//        endTime.set(Calendar.MINUTE, 59)
//        endTime.set(Calendar.SECOND, 59)
//
//
//        val caloriesRequest = DataReadRequest.Builder()
//            .setTimeRange(startTime.timeInMillis, endTime.timeInMillis, TimeUnit.MILLISECONDS)
//            .read(DataType.TYPE_CALORIES_EXPENDED)
//            .build()
//
//        println("calories request: $caloriesRequest")
//
//        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
//            .readData(caloriesRequest)
//            .addOnSuccessListener { response ->
//                val caloriesDataSet = response.getDataSet(DataType.TYPE_CALORIES_EXPENDED)
//                val calories = caloriesDataSet.dataPoints
//                println("calories: $calories")
//                for (calorie in calories) {
//                    for (field in calorie.dataType.fields) {
//                        val caloriesBurned = calorie.getValue(field).asFloat()
//                        totalCalories += caloriesBurned
//                        binding.tvCalories.text = totalCalories.toString()
//                        println("calories burned: $caloriesBurned")
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                println("failed to get calories burned: $e")
//            }
//
//
//    }

    private fun getStepCountForToday() {

        val stepCountRequest = SensorRequest.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setSamplingRate(1, TimeUnit.SECONDS)
            .build()

        val sensorClient =
            Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)

        sensorClient.add(stepCountRequest, stepCountListener)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("listener registered")
                } else {
                    println("listener not registered")
                }
            }
    }

    override fun onPause() {
        super.onPause()

        val editor = sharedPreferences.edit().apply {
            println("share pref set steps: $totalSteps + distance: $totalDistance")
            putInt("steps", totalSteps)
            putFloat("distance", totalDistance)
            putFloat("calories", totalCalories)
        }
        editor.apply()
    }

    override fun onResume() {
        super.onResume()

        val steps = sharedPreferences.getInt("steps", 0)
        val distance = sharedPreferences.getFloat("distance", 0f)
        val calories = sharedPreferences.getFloat("calories", 0f)
        println("steps $steps")
        binding.tvStepCount.text = steps.toString()
        binding.tvDistance.text = distance.toString()
        binding.tvCalories.text = calories.toString()
    }


    @SuppressLint("SetTextI18n")
    private fun getDistanceAndCal() {

        val startTime = Calendar.getInstance()
        startTime.set(Calendar.YEAR, 2023)
        startTime.set(Calendar.MONTH, Calendar.JULY)
        startTime.set(Calendar.DAY_OF_MONTH, 1)
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)

        val endTime = Calendar.getInstance()
        endTime.set(Calendar.YEAR, 2023)
        endTime.set(Calendar.MONTH, Calendar.JULY)
        endTime.set(Calendar.DAY_OF_MONTH, 10)
        endTime.set(Calendar.HOUR_OF_DAY, 23)
        endTime.set(Calendar.MINUTE, 59)
        endTime.set(Calendar.SECOND, 59)

        val googleApi = GoogleApiAvailability.getInstance()
        googleApi.isGooglePlayServicesAvailable(this)

        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
            .silentSignIn()
            .result as GoogleSignInAccount

        val distanceRequest = DataReadRequest.Builder()
            .setTimeRange(startTime.timeInMillis, endTime.timeInMillis, TimeUnit.MILLISECONDS)
            .read(DataType.TYPE_DISTANCE_DELTA)
            .build()

        val caloriesRequest = DataReadRequest.Builder()
            .setTimeRange(startTime.timeInMillis, endTime.timeInMillis, TimeUnit.MILLISECONDS)
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .build()


        val distanceTask = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
            .readData(distanceRequest)

        val caloriesTask = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
            .readData(caloriesRequest)


        Tasks.whenAll(distanceTask, caloriesTask)
            .addOnSuccessListener {
                val distanceDataSet = distanceTask.result?.getDataSet(DataType.TYPE_DISTANCE_DELTA)
                val caloriesDataSet = caloriesTask.result?.getDataSet(DataType.TYPE_CALORIES_EXPENDED)

                val distances = distanceDataSet?.dataPoints
                val calories = caloriesDataSet?.dataPoints

                for (distance in distances!!) {
                    for (field in distance.dataType.fields) {
                        val distanceInKm = distance.getValue(field).asFloat().div(1000)
                        val formattedDistance = String.format("%.2f", distanceInKm)
                        totalDistance +=  formattedDistance.toFloat()
                        println("total distance: $totalDistance")
                        binding.tvDistance.text = formattedDistance
                        println("distance in km: $distanceInKm")

                    }
                }

                for (calorie in calories!!) {
                    for (field in calorie.dataType.fields) {
                        val kCal = calorie.getValue(field).asFloat().div(1000)
                        val formattedCalories = String.format("%.2f", kCal)
                        totalCalories += formattedCalories.toFloat()
                        binding.tvCalories.text = formattedCalories
                        println("calories burned: $kCal")
                    }
                }
            }
            .addOnFailureListener { e ->
                println("failed to get calories burned: $e")
            }



    }

}