package com.example.gallery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gallery.databinding.ActivityMain2Binding
import com.example.gallery.services.MyService
import com.example.gallery.viewmodals.MusicPlayerViewModel
import com.example.gallery.viewmodals.StepCounterViewModel
import com.example.gallery.worker.MyWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.concurrent.TimeUnit

//, MusicPlayerCallback

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private var myService: MyService? = null
    private lateinit var player: MediaPlayer
    private var viewModel: MusicPlayerViewModel.Player = MusicPlayerViewModel.Player
    private lateinit var stepViewModel: StepCounterViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        println("on create")



        // Bind to the Service
        //val serviceIntent = Intent(this, MyService::class.java)
        //bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)


        initListener()

        //receiveBroadcast()
        val filter = IntentFilter("MusicData")
        LocalBroadcastManager.getInstance(this).registerReceiver(musicProgress, filter)
        //musicProgress
    }

    override fun onResume() {
        super.onResume()
        //val filter = IntentFilter("MusicData")
        //LocalBroadcastManager.getInstance(this).registerReceiver(musicProgress, filter)
    }


    private val musicProgress = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("here1")
            if (intent?.action == "MusicData") {
                println("here")
                val currentTime = intent.getIntExtra("currentTime", 0)
                val finalTime = intent.getIntExtra("finalTime", 0)
                val progress = intent.getIntExtra("progress", 0)
                val startTime = changeTimeFormat(currentTime)
                val endTime = changeTimeFormat(finalTime)
                binding.appCompatSeekBar.progress = progress

                binding.appCompatSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener{
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        //player.seekTo(progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        TODO("Not yet implemented")
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        TODO("Not yet implemented")
                    }

                })

                binding.startTime.text = startTime
                binding.endTime.text = endTime
                println("currentTime $currentTime")
                println("finalTime $finalTime")
                println("progress $progress")
            }
        }
    }

    private fun changeTimeFormat(time: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time.toLong()) -
                TimeUnit.MINUTES.toSeconds(minutes)


        return String.format("%02d:%02d", minutes, seconds)
    }




    private fun initListener() {
        //var isPlaying = false
        var isClicked = false
        val filename: String = "android.resource://" + this.packageName + "/raw/song"
        player = MusicPlayerViewModel.Player.getMediaPlayer()
        try {
            player.setDataSource(this, Uri.parse(filename))
        }
        catch (e: Exception){
            println("e: $e")
        }

        try {
            player.prepare()
        }catch (e: Exception){
            println("exception: $e")
        }
        println("player main $player")


        binding.play.setOnClickListener {

            if (isClicked) {
                // pause the music

                binding.play.setImageResource(R.drawable.play)
                //stopService(Intent(this, MyService::class.java))
                player.stop()
                isClicked = false
                //isPlaying = false
            } else {
                // play the music
                binding.play.setImageResource(R.drawable.pause)
                //startService(Intent(this, MyService::class.java))
                player.start()
                //player.isLooping = true
                isClicked = true
                //isPlaying = true
            }


        }


        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true).build()
        val request = OneTimeWorkRequestBuilder<MyWork>()
            .setConstraints(constraints = constraints).build()
        binding.textView2.setOnClickListener {
            WorkManager.getInstance(this).enqueue(request)
        }

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(request.id)
            .observe(this, Observer {
                val status = it.state.name
                Toast.makeText(this, "status $status", Toast.LENGTH_SHORT).show()
            })

        binding.sw1.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "switch: On" else "switch: Off"
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                //binding.imageView.setColorFilter(R.color.purple_200)
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        binding.next.setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(this,SettingsActivity::class.java))
        }

        binding.imageApp.setOnClickListener {
            startActivity(Intent(this,ImageActivity::class.java))
        }

        binding.googleFit.setOnClickListener {
            startActivity(Intent(this,FitActivity::class.java))
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        //player.stop()
        //player.release()
        stopService(Intent(this, MyService::class.java))
    }


    private fun receiveBroadcast() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                println("message received")
                val message = intent?.getStringExtra("message")

                // Handle the received message here
                println("message $message")
            }
        }
        val filter = IntentFilter("com.example.gallery.MAIN_ACTIVITY")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

//    override fun onCurrentTimeChanged(currentTime: Long) {
//        println("currentTime : $currentTime")
//    }

    //    private val serviceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            // Called when the Service is connected
//            val binder = service as MyService.MyBinder
//            myService = binder.getService()
//
//            // Register the Activity as a callback
//            myService?.registerCallback(this@MainActivity2)
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            // Called when the Service is disconnected (unexpectedly)
//            stopService(Intent(this@MainActivity2, MyService::class.java))
//            myService = null
//        }
//    }








}