package com.example.gallery.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gallery.models.Steps


@Dao
interface StepsDao {

    @Insert
    fun insert(steps: Steps)

    @Update
    fun update(steps: Steps)

    @Query("SELECT * FROM steps_table")
    fun getSteps(): List<Steps>
}