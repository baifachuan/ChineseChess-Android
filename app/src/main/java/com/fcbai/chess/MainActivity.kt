package com.fcbai.chess

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {
//    var player: MediaPlayer? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
//        val startSound = MediaPlayer.create(this, R.raw.startsound)
//        startSound.isLooping = true
//        player = startSound
//        player?.start()
        setContentView(R.layout.activity_main)
        setButtonEvent()
        val constraintLayout = findViewById<ConstraintLayout>(R.id.main_layout)
        constraintLayout.children.forEach { f ->
            f.setOnTouchListener { v, event ->
                if (v is ImageButton) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            v.setScaleX(0.95F)
                            v.setScaleY(0.95F)
                        }
                        MotionEvent.ACTION_UP -> {
                            v.setScaleX(1.0F)
                            v.setScaleY(1.0F)
                        }

                    }
                }
                v?.onTouchEvent(event) ?: false
            }
        }

    }

    private fun setButtonEvent() {
        val closeSoundButton = findViewById<ImageButton>(R.id.close_sound)
        closeSoundButton.setOnClickListener { v ->
            v?.isVisible ?: false
            findViewById<ImageButton>(R.id.open_sound).isVisible = true
//            if (player?.isPlaying != false) player?.stop()
        }


        val openSoundButton = findViewById<ImageButton>(R.id.open_sound)
        openSoundButton.setOnClickListener { v ->
            v?.isVisible ?: false
            findViewById<ImageButton>(R.id.close_sound).isVisible = true
//            if (player?.isPlaying != true) player?.start()
        }

        val startGameButton = findViewById<ImageButton>(R.id.start_game)
        startGameButton.setOnClickListener { v ->
//            player?.stop()
            startActivity(Intent(MainActivity@this, GameActivity::class.java))
        }
    }
}
