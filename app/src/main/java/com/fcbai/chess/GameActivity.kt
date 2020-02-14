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
import com.google.gson.Gson
import org.apache.http.params.HttpConnectionParams.setConnectionTimeout
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage


class GameActivity: AppCompatActivity() {

    var onClickMediaPlayer: MediaPlayer? = null

    companion object {
        const val AI_UPDATE = 1001
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            val notificationMessage = Klaxon().parse<NotificationMessage>(msg?.obj.toString())
            notificationMessage?.let {

                Model.getChessPiece(it.to.biasX, it.to.biasY)?.let { exist ->
                    findViewById<ImageButton>(exist.id).visibility = View.INVISIBLE
                    exist.isDeath = true
                }

                Model.getChessPiece(it.from.biasX, it.from.biasY)?.let { exist ->
                    val view = findViewById<ImageButton>(exist.id)
                    val layout = view.layoutParams as ConstraintLayout.LayoutParams
                    layout.verticalBias = it.to.biasY
                    layout.horizontalBias = it.to.biasX
                    view.layoutParams = layout
                    exist.position = it.to
                    onClickMediaPlayer?.start()
                }

            }

        }
    }


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

        val client = MqttAndroidClient(applicationContext, "tcp://10.0.2.2:1883", MqttAsyncClient.generateClientId())

        // 设置MQTT监听并且接受消息
        client.setCallback(MQttCallbackHandler(mHandler))
        //Mqtt的一些设置
        val conOpt = MqttConnectOptions()
        conOpt.setAutomaticReconnect(true)
        // 清除缓存
        conOpt.setCleanSession(true)
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10)
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20)

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
                redesigned.setOnClickListener { view ->
                    run {

                        if (StatusModel.stepMessage.step == Step.AI) return@run
                        val onClickChessPiece = Model.getChessPiece(view.id)

                        val to = Position(
                            biasY = onClickChessPiece.position.biasY,
                            biasX = onClickChessPiece.position.biasX,
                            x = onClickChessPiece.position.x,
                            y = onClickChessPiece.position.y
                        )

                        val toLayout = view.layoutParams as ConstraintLayout.LayoutParams

                        //这里需要检查，吃掉对方棋子的情况
                        StatusModel.getChessPieceEvent()?.let {

                            val from = it.chessPiece.position

                            when (Model.updateChessBoard(it.chessPiece.id, to)) {

                                ActionStatus.SUCCESS -> {
                                    val fromView = findViewById<ImageButton>(it.chessPiece.id)
                                    view.visibility = View.INVISIBLE
                                    Model.getChessPiece(view.id).isDeath = true
                                    val fromLayout = fromView.layoutParams as ConstraintLayout.LayoutParams
                                    fromLayout.verticalBias = toLayout.verticalBias
                                    fromLayout.horizontalBias = toLayout.horizontalBias
                                    fromView.layoutParams = fromLayout
                                    onClickMediaPlayer?.start()

                                    it.chessPiece.position = to

                                    val blinkView = StatusModel.peekBlinkView()
                                    blinkView?.clearAnimation()
                                    blinkView?.animation?.cancel()
                                    StatusModel.clearQueue()
                                    StatusModel.stepMessage = NotificationMessage(from, to, Step.AI)
                                }
                                else -> {
                                    StatusModel.putEvent(ChessPieceEvent(Model.getChessPiece(view.id)))
                                }
                            }
                        } ?: run {
                            StatusModel.putEvent(ChessPieceEvent(Model.getChessPiece(view.id)))
                        }

                        if (onClickChessPiece.group != StatusModel.gameInfo.group) return@run
                        val blinkView = StatusModel.peekBlinkView()
                        blinkView?.let {
                            blinkView.clearAnimation()
                            blinkView.animation?.cancel()
                            if (blinkView.id != view.id) {
                                view.startAnimation(ViewHelper.getAlphaAnimationForBlink())
                            } else {
                                view.startAnimation(ViewHelper.getAlphaAnimationForBlink())
                            }
                        } ?: run {
                            view.startAnimation(ViewHelper.getAlphaAnimationForBlink())
                        }
                        StatusModel.putBlinkView(view)
                    }
                }

            }
        }


        constraintLayout.setOnTouchListener { v, event ->

            if (StatusModel.stepMessage.step == Step.AI) return@setOnTouchListener v?.onTouchEvent(event) ?: false

            val displayMetrics = DisplayMetrics()
            this.windowManager.defaultDisplay.getMetrics(displayMetrics)
            StatusModel.getChessPieceEvent()?.let {
                val fromView = findViewById<ImageButton>(it.chessPiece.id)
                val to = Model.getNearPosition(event.x, event.y)
                if (Model.getChessPiece(fromView.id).group != StatusModel.gameInfo.group) return@let
                when (Model.updateChessBoard(fromView.id, to)) {
                    ActionStatus.SUCCESS -> {
                        val toLayout = fromView.layoutParams as ConstraintLayout.LayoutParams
                        Log.d("search position", "chessPieceEvent.verticalBias:" + to.biasY + "    chessPieceEvent.verticalBias: " + to.biasX)
                        toLayout.verticalBias = to.biasY
                        toLayout.horizontalBias = to.biasX
                        fromView.layoutParams = toLayout

                        val fromPos = Model.getChessPiece(fromView.id).position
                        val from = Model.getPosition(fromPos.biasX, fromPos.biasY)
                        Model.getChessPiece(fromView.id).position = to
                        onClickMediaPlayer?.start()
                        StatusModel.clearQueue()
                        StatusModel.stepMessage = NotificationMessage(from!!, to, Step.AI)
                    }
                    else -> {
                        Toast.makeText(this, "不合法的位置", Toast.LENGTH_SHORT).show()
                    }
                }

                val blinkView = StatusModel.peekBlinkView()
                blinkView?.clearAnimation()
                blinkView?.animation?.cancel()
            }

            v?.onTouchEvent(event) ?: false
        }
        LoginAsyncTask().execute()
        AIInvokeAsyncTask(mHandler, client).execute()
    }

}