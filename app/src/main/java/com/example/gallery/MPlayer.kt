package com.example.gallery

import android.media.MediaPlayer

class MPlayer private constructor(){
    companion object{
        private var mediaPlayer: MediaPlayer? = null

        fun getInstance(): MediaPlayer {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            return mediaPlayer!!
        }
    }
}