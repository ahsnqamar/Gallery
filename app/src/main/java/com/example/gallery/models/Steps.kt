package com.example.gallery.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps_table")
data class Steps(
    val stepCount: Int,
    val distance: Double,
    val date: String,
    val caloriesBurned: Double,
    val totalMoveMinutes: Int,
    @PrimaryKey(autoGenerate = false) val id: Int ?= null
    ) {
}