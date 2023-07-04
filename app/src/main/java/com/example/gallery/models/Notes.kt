package com.example.gallery.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class Notes(
    val heading: String,
    val text: String,
    val date: String,
    @PrimaryKey(autoGenerate = false) val id: Int ?= null
)
