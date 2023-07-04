package com.example.gallery

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gallery.databinding.ActivityEditNotesBinding
import com.example.gallery.databinding.ActivityNotesBinding

class EditNotes : AppCompatActivity() {

    private lateinit var binding: ActivityEditNotesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submit.setOnClickListener {
            handleEditText()
        }

    }

    private fun handleEditText(){
        val heading = binding.noteHeading.text
        val notesText = binding.noteText.text

        val resultIntent = Intent()
        resultIntent.putExtra("heading", heading.toString())
        resultIntent.putExtra("text",notesText.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()

    }
}