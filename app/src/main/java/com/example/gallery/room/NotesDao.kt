package com.example.gallery.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gallery.models.Notes

@Dao
interface NotesDao {

    @Insert
    fun insert(note:Notes)
    @Update
    fun update(note: Notes)

    @Query("DELETE FROM note_table")
    fun delete()

    @Delete
    fun deleteOne(note: Notes)

    @Query("select * from note_table")
    fun getNotes(): List<Notes>

}