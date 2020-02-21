package com.fcbai.chess

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.beust.klaxon.Klaxon
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions


class GameActivity: AppCompatActivity() {

    var onClickMediaPlayer: MediaPlayer? = null
    var loadDialog: LoadDialog? = null

    companion object {
        const val AI_UPDATE = 1001
        const val COUNT_DOWN = 1002
        const val GAME_INIT_SUCCESS = 1003

    }

    private val mHandler: Handler = NetworkHelper.getHandler(this)


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        onClickMediaPlayer = MediaPlayer.create(this, R.raw.go)
        onClickMediaPlayer?.isLooping = false

        val constraintLayout = findViewById<ConstraintLayout>(R.id.game_panel)
        val displayMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(displayMetrics)

        loadDialog = LoadDialog(this@GameActivity)
        loadDialog!!.show()

        val client = MqttAndroidClient(applicationContext, ConfigHelper.getValue(this@GameActivity, "mqtt.uri"), MqttAsyncClient.generateClientId())

        client.setCallback(MQttCallbackHandler(mHandler))
        val conOpt = MqttConnectOptions()
        conOpt.isAutomaticReconnect = true
        conOpt.isCleanSession = true
        conOpt.connectionTimeout = 10
        conOpt.keepAliveInterval = 20

        client.connect(conOpt, null, MQTTIMqttActionListener())

        StatusModel.absolutePosition = AbsolutePosition(
            displayMetrics.widthPixels, displayMetrics.heightPixels,
            79, 79,
            constraintLayout.width - 20, constraintLayout.height / 3 * 2)

        StatusModel.gameInfo = GameInfo(1, Group.RED)

        if (StatusModel.gameInfo.group != Group.RED) {
            val reds = Model.getDefaultChessBoard().flatten().filter { f -> f.group ==Group.RED }
            val blacks = Model.getDefaultChessBoard().flatten().filter { f -> f.group ==Group.BLACK }
            reds.forEach { f -> f.group = Group.BLACK }
            blacks.forEach { f -> f.group = Group.RED }
        }

        Model.getDefaultChessBoard().map { f1 ->
            f1.map { f ->
                val redesigned = findViewById<ImageButton>(f.id)
                redesigned.background = BitmapDrawable(
                    resources,
                    ViewHelper.combineImage(BitmapFactory.decodeResource(
                        resources, R.drawable.qizi), BitmapFactory.decodeResource(
                        resources, Model.getResource(f.group, f.chessPieceType))
                    )
                )
                val redesignedLayoutParams = redesigned.layoutParams as ConstraintLayout.LayoutParams
                redesignedLayoutParams.horizontalBias = f.position.biasX
                redesignedLayoutParams.verticalBias = f.position.biasY
                redesigned.layoutParams = redesignedLayoutParams
                redesigned.setOnClickListener (ButtonOnClickListener(this))
            }
        }

        constraintLayout.setOnTouchListener(LayoutOnTouchListener(this))
        LoginAsyncTask(mHandler, this@GameActivity).execute()
        AIInvokeAsyncTask(mHandler, client).execute()
    }

}