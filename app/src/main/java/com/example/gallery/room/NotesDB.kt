package com.example.gallery.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gallery.models.Notes

@Database(entities = [Notes::class], version = 1)
abstract class NotesDB: RoomDatabase() {
    abstract fun noteDao(): NotesDao

    companion object {
        private var instance: NotesDB ?= null

        @Synchronized
        fun getInstance(ctx: Context): NotesDB {
            if (instance == null){
                instance = Room.databaseBuilder(ctx.applicationContext, NotesDB::class.java,
                    "note_database")
                    .build()
            }

            return  instance!!
        }
    }

}