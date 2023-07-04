package com.example.gallery.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.R
import com.example.gallery.models.Notes

class NotesAdapter (private val mNotes: List<Notes>): RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {

        val heading: TextView = itemView.findViewById(R.id.heading)
        val notesText: TextView = itemView.findViewById(R.id.note_text)
        val noteDate: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notes,parent,false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notesModel = mNotes[position]
        holder.heading.text = notesModel.heading
        holder.notesText.text = notesModel.text
        holder.noteDate.text = notesModel.date

    }
}