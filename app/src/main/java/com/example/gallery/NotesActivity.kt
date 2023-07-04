package com.example.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gallery.adapters.NotesAdapter
import com.example.gallery.databinding.ActivityMainBinding
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
    private var heading: String ?= null
    private  var text: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()

        binding.notesRv.layoutManager = LinearLayoutManager(this)
        //handleRoomDatabase()
//        val data = ArrayList<Notes>()
//        for (i in 1..20){
//            data.add(Notes("heading $i", "text $i", " $i"))
//        }
//        println("size ${data.size}")
//        val adapter = NotesAdapter(data)
//        binding.notesRv.adapter = adapter
    }

    private fun initListener() {
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK){
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

    private fun populateDB(db: NotesDB){
        val noteDao = db.noteDao()
        heading?.let { text?.let { it1 -> Notes(it, it1,current) } }?.let { noteDao.insert(it) }
        //noteDao.delete()
//        for (i in 1..20){
//            noteDao.insert(Notes("heading $i", "text $i", current))
//        }
//        binding.addNotes.setOnClickListener {
//            noteDao.insert(Notes("any","any",current))
//        }

    }
    private fun handleRoomDatabase(){
        val database = NotesDB.getInstance(applicationContext)
        val notesDao: NotesDao = database.noteDao()
        //notesDao.delete()
        CoroutineScope(Dispatchers.IO).launch{
            populateDB(NotesDB.getInstance(this@NotesActivity))
            val data = notesDao.getNotes()
            println("data $data")
            withContext(Dispatchers.Main){
                val adapter = NotesAdapter(data)
                binding.notesRv.adapter = adapter
            }
        }
    }
}