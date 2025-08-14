package com.hereliesaz.lefauxpass

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var timeTextView: TextView
    private lateinit var expirationTextView: TextView
    private lateinit var videoView: VideoView
    private var expirationTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timeTextView = findViewById(R.id.timeTextView)
        expirationTextView = findViewById(R.id.expirationTextView)
        videoView = findViewById(R.id.videoView)

        // Replace "your_video_path" with the actual path to your video
        videoView.setVideoPath("app/src/main/res/raw/rtalogo.mp4")
        videoView.start()
        videoView.setOnCompletionListener {
            videoView.start()
        }

        val currentTime = System.currentTimeMillis()
        expirationTime = currentTime + (3600 + 57 * 60 + 13) * 1000
        updateTimer()

        object : CountDownTimer(expirationTime - currentTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimer()
            }

            override fun onFinish() {
                // Handle expiration here (e.g., show a message)
            }
        }.start()
    }

    private fun updateTimer() {
        val currentTime = System.currentTimeMillis()
        val remainingTime = expirationTime - currentTime
        val hours = remainingTime / (1000 * 60 * 60)
        val minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (remainingTime % (1000 * 60)) / 1000

        timeTextView.text = SimpleDateFormat("HH:mm:ss").format(Date())
        expirationTextView.text = "Expires in: $hours hours, $minutes minutes, $seconds seconds"
    }
}


