package com.fcbai.chess

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {
    var player: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val startSound = MediaPlayer.create(this, R.raw.startsound)
        startSound.isLooping = true
        player = startSound
        player?.start()
        setContentView(R.layout.activity_main)
        setButtonEvent()
    }

    private fun setButtonEvent() {
        val closeSoundButton = findViewById<ImageButton>(R.id.close_sound)
        closeSoundButton.setOnClickListener { v ->
            v?.isVisible ?: false
            findViewById<ImageButton>(R.id.open_sound).isVisible = true
            if (player?.isPlaying != false) player?.stop()
        }


        val openSoundButton = findViewById<ImageButton>(R.id.open_sound)
        openSoundButton.setOnClickListener { v ->
            v?.isVisible ?: false
            findViewById<ImageButton>(R.id.close_sound).isVisible = true
            if (player?.isPlaying != true) player?.start()
        }

        val startGameButton = findViewById<ImageButton>(R.id.start_game)
        startGameButton.setOnClickListener { v ->
            player?.stop()
            startActivity(Intent(MainActivity@this, GameActivity::class.java))
        }
    }
}
