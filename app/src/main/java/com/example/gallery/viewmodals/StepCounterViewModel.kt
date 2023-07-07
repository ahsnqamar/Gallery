package com.example.gallery.viewmodals

import android.content.SharedPreferences
import android.view.View
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepCounterViewModel : ViewModel() {




    private val _stepCountLiveData = MutableLiveData<Int>()
    val stepCountLiveData: LiveData<Int> get() = _stepCountLiveData

    private val _distanceLiveData = MutableLiveData<Double>()
    val distanceLiveData: LiveData<Double> get() = _distanceLiveData

    private val _dateLiveData = MutableLiveData<String>()
    val dateLiveData: LiveData<String> get() = _dateLiveData

    private val _caloriesBurnedLiveData = MutableLiveData<Double>()
    val caloriesBurnedLiveData: LiveData<Double> get() = _caloriesBurnedLiveData

    fun updateStepCount(stepCount: Int) {
        _stepCountLiveData.value = stepCount
    }

    fun updateDistance(distance: Double) {
        _distanceLiveData.value = distance
    }

    fun updateDate(date: String) {
        _dateLiveData.value = date
    }

    fun updateCaloriesBurned(caloriesBurned: Double) {
        _caloriesBurnedLiveData.value = caloriesBurned
    }
}
