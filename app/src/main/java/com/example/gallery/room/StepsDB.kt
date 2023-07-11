package com.example.gallery.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gallery.models.Steps

@Database(entities = [Steps::class], version = 1)
abstract class StepsDB: RoomDatabase() {

    abstract fun stepDao(): StepsDao

    companion object {
        private var instance: StepsDB ?= null

        @Synchronized
        fun getInstance(ctx: Context): StepsDB {
            if (instance == null){
                instance = Room.databaseBuilder(ctx.applicationContext, StepsDB::class.java,
                    "steps_database")
                    .build()
            }

            return  instance!!
        }
    }


}