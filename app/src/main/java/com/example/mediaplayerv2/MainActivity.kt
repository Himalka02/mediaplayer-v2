package com.example.mediaplayerv2

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var audioUri: Uri? = null
    private var currentAudioIndex = 0
    private var audioUris: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        val btnPlay: Button = findViewById(R.id.btnPlay)
        val btnPause: Button = findViewById(R.id.btnPause)
        val btnStop: Button = findViewById(R.id.btnStop)
        val btnNext: Button = findViewById(R.id.btnNext)
        val btnPrev: Button = findViewById(R.id.btnPrev)

        // Load audio files from the device
        loadAudioFiles()

        btnPlay.setOnClickListener {
            if (!isPlaying) {
                playAudio(currentAudioIndex)
            } else {
                Toast.makeText(this, "Music is already playing", Toast.LENGTH_SHORT).show()
            }
        }

        btnPause.setOnClickListener {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            }
        }

        btnStop.setOnClickListener {
            if (isPlaying) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                isPlaying = false
            }
        }

        btnNext.setOnClickListener {
            playNextAudio()
        }

        btnPrev.setOnClickListener {
            playPreviousAudio()
        }
    }

    private fun loadAudioFiles() {
        // Query for audio files and get all of them
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val audioIndex = it.getColumnIndex(MediaStore.Audio.Media._ID)
                if (audioIndex != -1) {
                    val audioId = it.getLong(audioIndex)
                    val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId.toString())
                    audioUris.add(uri)
                } else {
                    Toast.makeText(this, "Audio column not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAudio(index: Int) {
        audioUris.getOrNull(index)?.let {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MainActivity, it)
                prepare()
                start()
                this@MainActivity.isPlaying = true
            }
        } ?: Toast.makeText(this, "No audio file found", Toast.LENGTH_SHORT).show()
    }

    private fun playNextAudio() {
        currentAudioIndex = (currentAudioIndex + 1) % audioUris.size
        playAudio(currentAudioIndex)
    }

    private fun playPreviousAudio() {
        currentAudioIndex = (currentAudioIndex - 1 + audioUris.size) % audioUris.size
        playAudio(currentAudioIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
                loadAudioFiles() // Load audio files again if permission granted
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}