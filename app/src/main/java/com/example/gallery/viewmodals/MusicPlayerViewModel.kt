package com.example.gallery.viewmodals

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gallery.R

class MusicPlayerViewModel() : ViewModel() {

    object Player {
        private var mediaPlayer: MediaPlayer ?= null
        public var image: Int ?= null

        fun getMediaPlayer(): MediaPlayer {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            return mediaPlayer!!
        }
    }

}