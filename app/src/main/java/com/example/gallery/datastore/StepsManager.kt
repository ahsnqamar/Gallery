package com.example.gallery.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.stepCounterStore: DataStore<Preferences> by preferencesDataStore("steps_counter")

class StepsManager(context: Context) {
    private val dataStore = context.stepCounterStore
    private val stepsKey = intPreferencesKey("steps")

    val stepsFlow = dataStore.data.map { preferences ->
        preferences[stepsKey] ?: 0
    }

    suspend fun updateSteps(steps: Int) {
        dataStore.edit { preferences ->
            preferences[stepsKey] = steps
        }
    }
}