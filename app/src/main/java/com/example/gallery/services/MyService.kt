package com.example.gallery.services

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.SeekBar
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gallery.MPlayer
import com.example.gallery.MainActivity2
import com.example.gallery.R
import com.example.gallery.viewmodals.MusicPlayerViewModel
import java.io.File
import java.lang.Exception


interface MusicPlayerCallback {
    fun onCurrentTimeChanged(currentTime: Long)
}

class MyService: Service() {


    private lateinit var player: MediaPlayer
    private var currentTime: Int ?= null
    private var endTime: Int ?= null
    private var musicPlayerCallback: MusicPlayerCallback? = null
    private lateinit var seekBar: SeekBar

    inner class MyBinder: Binder(){
        fun getService(): MyService{
            return this@MyService
        }
    }

    fun registerCallback(callback: MusicPlayerCallback) {
        musicPlayerCallback = callback
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        val filename: String = "android.resource://" + this.packageName + "/raw/song"
        player = MusicPlayerViewModel.Player.getMediaPlayer()
//        try {
//            player.setDataSource(this, Uri.parse(filename))
//        }
//        catch (e: Exception){
//            println("e: $e")
//        }
//
//        try {
//            player.prepare()
//        }catch (e: Exception){
//            println("exception: $e")
//        }
        println("player $player")
//        player.start()
//
//
//        player.isLooping = true
//        if (!player.isPlaying)
//            player.start()

        currentTime = player.currentPosition
        endTime = player.duration


        println("Time: $endTime")
        sendData()
        handler.postDelayed(updateRunnable,1000)

        musicPlayerCallback?.onCurrentTimeChanged(currentTime = currentTime!!.toLong())
        sendNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }


    private fun handleSeekBar(): Int {
        seekBar = SeekBar(this)
        seekBar.max = (player.duration/1000)
        seekBar.progress = player.currentPosition / 1000

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                player.seekTo(progress*1000)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }

        })

        return seekBar.progress
    }

    private fun calculateNewSongDuration(progress: Int): Int {
        // Calculate the new song duration based on the seek bar progress
        // Adjust the logic based on your specific requirements
        val maxDuration = player.duration
        return (maxDuration * (progress.toFloat() / seekBar.max)).toInt()
    }



    override fun onCreate() {
        super.onCreate()
        println("on create")



    }

    private fun sendData(){
        if (player.isPlaying){
            currentTime = player.currentPosition
            endTime = player.duration

            val progress = handleSeekBar()
            val intent = Intent("MusicData")
            intent.putExtra("currentTime", currentTime)
            intent.putExtra("finalTime", endTime)
            intent.putExtra("progress", progress)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        }

    }

    private val handler = Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            //sendData()
            handler.postDelayed(this, 1000)
        }
    }


    private fun sendNotification(){
        // Step 1: Define the Notification Channel (if needed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Step 2: Create the Notification
        val channelId = "my_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Music Player")
            .setContentText("Music is playing...")
            .setSmallIcon(R.drawable.play)

        // Step 3: Create a Pending Intent
        val notificationIntent = Intent(this, MainActivity2::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.setContentIntent(pendingIntent)

        // Step 4: Start the Service as a Foreground Service
        val notification = notificationBuilder.build()
        startForeground(101, notification)

    }



}