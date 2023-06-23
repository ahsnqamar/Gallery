package com.example.gallery.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //println("message received")
        val message = intent?.getStringExtra("message")
        // Handle the received message here
        println("message $message")
        Toast.makeText(context,"$message",Toast.LENGTH_SHORT).show()
    }
}