package com.example.gallery

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gallery.databinding.ActivitySettingsBinding
import java.net.URLDecoder

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()


    }

    private fun initListener() {
        binding.imageView10.setOnClickListener {
            // get ringtone
            val currentRingTone: Uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingTone)
            startActivityForResult(intent, 5)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 5) {
                val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    val ringTone: String = uri.toString()
                    val ringToneName = extractRingtoneName(ringTone)
                    binding.toneText.text = ringToneName
                    println("ringtone $ringTone")
                }
            }
    }

    private fun extractRingtoneName(urlString: String): String? {
        val url = URLDecoder.decode(urlString, "UTF-8")
        val queryParameters = url.split("?").getOrNull(1)?.split("&")
        val titleParameter = queryParameters?.find { it.startsWith("title=") }
        return titleParameter?.substringAfter("title=")
    }

}