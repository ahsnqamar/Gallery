package com.example.gallery

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gallery.adapters.GridViewAdapter
import com.example.gallery.databinding.ActivityMainBinding
import com.example.gallery.models.GridModal
import com.example.gallery.receivers.MyReceiver
import com.example.gallery.ui.theme.GalleryTheme
import com.example.gallery.viewmodals.MyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.jar.Manifest

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var images: ArrayList<GridModal>
    private val myReceiver: BroadcastReceiver = MyReceiver()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission(android.Manifest.permission.READ_MEDIA_IMAGES,101)
        checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,100)
        var viewModel : MyViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        init()
        initListener()

        handleViewModel(viewModel)
        //images = images + GridModal(R.drawable.search)

        val imagePaths = getAllImages(applicationContext)

        for (path in imagePaths){
            //println("path $path")
            val gridModal = GridModal(path)
            //println("gridModal $gridModal")
            images.add(gridModal)
        }

        //val data = getAllImages(context = applicationContext)
        //println("data $images")

        val gridAdapter = GridViewAdapter(images, this)
        binding.gridView.adapter = gridAdapter

        sendBroadcast()

        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, IntentFilter("com.example.gallery.MAIN_ACTIVITY"))


    }

    private fun initListener(){
        binding.textView3.setOnClickListener {
            startActivity(Intent(this,MainActivity2::class.java))
        }
    }

    private fun sendBroadcast(){
        val intent = Intent("com.example.gallery.MAIN_ACTIVITY")
        intent.putExtra("message","hello world")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        println("message sent")
        //startActivity(Intent(this,MainActivity2::class.java))
    }

    private fun handleViewModel(viewModel: MyViewModel){
        binding.textView.text = viewModel.number.toString()
        binding.searchIcon.setOnClickListener {
            viewModel.addOne()
            binding.textView.text = viewModel.number.toString()
        }
    }

    private fun init() {
        images = ArrayList<GridModal>()
        //learnCoroutines()
        //touchListener()
    }

    private fun checkPermission(permission: String,requestCode: Int ){
        if (ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_DENIED){
            // request for permission
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }else{
            //showToast("permission already granted")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 101){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //showToast("Permission granted")
            }
            else{
                //showToast("Permission denied")
            }
        }
        if(requestCode == 100){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //showToast("Permission granted")
            }
            else{
                //showToast("Permission denied")
            }
        }
    }

    private fun showToast(msg: String){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }

    private fun getAllImages(context: Context): ArrayList<String> {
        val imagePaths = ArrayList<String>()

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use { c ->
            val dataColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (c.moveToNext()) {
                val data = c.getString(dataColumn)
                imagePaths.add(data)
            }
        }

        return imagePaths
    }

//    private fun learnCoroutines(){
//        runBlocking{
//            coroutineScope {
//                val task1 = launch {
//                    println("Task 1 started")
//                    delay(100)
//                    //println("Task 1 completed!")
//                }
//                val task2 = launch {
//                    println("Task 2 started")
//                    delay(1000)
//                    println("Task 2 completed!")
//                }
//                task1.cancel()
//                listOf(task1, task2).joinAll()
//                println("Finished waiting for both tasks")
//            }
//
//            println("done")
//        }
//    }

//    @SuppressLint("ClickableViewAccessibility")
//    private fun touchListener(){
//        binding.searchIcon.setOnTouchListener { v, event ->
//            val x = event?.x?.toInt()
//            val y = event?.y?.toInt()
//            when (event?.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    showToast("action down")
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    showToast("action up")
//                }
//
//                MotionEvent.ACTION_MOVE -> {
//                    showToast("moving x: $x , y: $y")
//                }
//            }
//            true
//        }
//
//    }


}
