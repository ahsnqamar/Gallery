package com.example.gallery.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.R
import com.example.gallery.databinding.NotesBinding
import com.example.gallery.models.Notes

class NotesAdapter() : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    var deleteListener: OnDeleteClickListener? = null
    private val mNotes: MutableList<Notes> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(NotesBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notesModel = mNotes[position]

        with(holder.binding) {
            heading.text = notesModel.heading
            noteText.text = notesModel.text
            date.text = notesModel.date

            delete.setOnClickListener {
                //val item = getItemId(position)
                deleteListener?.onDeleteClick(notesModel)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return mNotes.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(notesList: List<Notes>) {
        mNotes.clear()
        mNotes.addAll(notesList)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: NotesBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnDeleteClickListener {
        fun onDeleteClick(notes: Notes)
    }

}