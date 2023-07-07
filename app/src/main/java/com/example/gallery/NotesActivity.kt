package com.example.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gallery.adapters.NotesAdapter
import com.example.gallery.databinding.ActivityNotesBinding
import com.example.gallery.models.Notes
import com.example.gallery.room.NotesDB
import com.example.gallery.room.NotesDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private var heading: String? = null
    private var text: String? = null

    private val adapter by lazy { NotesAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.settings)

        init()
        initListener()

        binding.notesRv.layoutManager = LinearLayoutManager(this)
        handleRoomDatabase()
    }


    private fun init() {
        binding.notesRv.adapter = adapter
        adapter.deleteListener = object : NotesAdapter.OnDeleteClickListener {
            override fun onDeleteClick(notes: Notes) {
                CoroutineScope(Dispatchers.IO).launch {
                    val noteDao = NotesDB.getInstance(this@NotesActivity).noteDao()
                    val data = noteDao.getNotes()
                    println("before delete $data")
                    noteDao.deleteOne(note = notes )
                    val deleted = noteDao.getNotes()
                    println("after delete $deleted")
                    withContext(Dispatchers.Main){
                        adapter.setData(deleted)
                    }
                }
            }
        }
    }

    private fun initListener() {
        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    // get result
                    heading = it.data?.getStringExtra("heading").toString()
                    text = it.data?.getStringExtra("text").toString()
                    println("received values $heading and $text")
                    handleRoomDatabase()
                }
            }


        binding.addNotes.setOnClickListener {
            startForResult.launch(Intent(this, EditNotes::class.java))
        }
    }

    private val time: Date = Calendar.getInstance().time

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("yyyy-MM-dd")
    private val current: String = formatter.format(time)

    private fun populateDB(db: NotesDB) {
        val noteDao = db.noteDao()
        heading?.let { text?.let { it1 -> Notes(it, it1, current) } }?.let { noteDao.insert(it) }

        //noteDao.delete()

    }

    private fun handleRoomDatabase() {
        val database = NotesDB.getInstance(applicationContext)
        val notesDao: NotesDao = database.noteDao()
        //notesDao.delete()
        CoroutineScope(Dispatchers.IO).launch {
            populateDB(NotesDB.getInstance(this@NotesActivity))
            val data = notesDao.getNotes()
            println("data $data")
            withContext(Dispatchers.Main) {
                adapter.setData(data)
            }
        }
    }
}