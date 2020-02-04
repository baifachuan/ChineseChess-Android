package com.fcbai.chess

import android.annotation.SuppressLint
import android.graphics.*
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


class GameActivity: AppCompatActivity() {

    var onClickMediaPlayer: MediaPlayer? = null

    companion object {
        const val ROBOT_UPDATE = 1001
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                ROBOT_UPDATE -> {
                    val view = findViewById<ImageButton>(StatusModel.robotStepMessage.id)
                    val fromChessPieceLayoutParams = view.layoutParams as ConstraintLayout.LayoutParams
                    fromChessPieceLayoutParams.verticalBias = StatusModel.robotStepMessage.targetPosition.verticalBias
                    fromChessPieceLayoutParams.horizontalBias = StatusModel.robotStepMessage.targetPosition.horizontalBias
                    view.layoutParams = fromChessPieceLayoutParams
                    onClickMediaPlayer?.start()
                }
                else -> {
                    Log.e("mHandler", "nothing")
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

        StatusModel.absolutePosition = AbsolutePosition(
            displayMetrics.widthPixels, displayMetrics.heightPixels,
            79, 79,
            constraintLayout.width - 20, constraintLayout.height / 3 * 2)


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
                        if (StatusModel.stepMessage.step == Step.ROBOT) return@run
                        val onClickChessPiece = Model.getChessPieceById(view.id)

                        val onClickViewLayoutParams = view.layoutParams as ConstraintLayout.LayoutParams

                        //这里需要检查，吃掉对方棋子的情况
                        StatusModel.getChessPieceEvent()?.let {
                            when (Model.updateChessBoard(
                                it.chessPiece.id,
                                ChessPiecePosition(onClickChessPiece.position.x, onClickChessPiece.position.y, onClickChessPiece.position.biasY, onClickChessPiece.position.biasX, onClickChessPiece.id))
                                ) {
                                ActionStatus.SUCCESS -> {
                                    val fromChessPiece = findViewById<ImageButton>(it.chessPiece.id)
                                    view.visibility = View.INVISIBLE
                                    Model.getChessPieceById(view.id).isDeath = true
                                    val fromChessPieceLayoutParams = fromChessPiece.layoutParams as ConstraintLayout.LayoutParams
                                    fromChessPieceLayoutParams.verticalBias = onClickViewLayoutParams.verticalBias
                                    fromChessPieceLayoutParams.horizontalBias = onClickViewLayoutParams.horizontalBias
                                    fromChessPiece.layoutParams = fromChessPieceLayoutParams
                                    onClickMediaPlayer?.start()
                                    Model.getChessPieceById(fromChessPiece.id).position.x = Model.getChessPieceById(view.id).position.x
                                    Model.getChessPieceById(fromChessPiece.id).position.y = Model.getChessPieceById(view.id).position.y
                                    Model.getChessPieceById(fromChessPiece.id).position.biasX = Model.getChessPieceById(view.id).position.biasX
                                    Model.getChessPieceById(fromChessPiece.id).position.biasY = Model.getChessPieceById(view.id).position.biasY
                                    val blinkView = StatusModel.peekBlinkView()
                                    blinkView?.clearAnimation()
                                    blinkView?.animation?.cancel()
                                    StatusModel.clearQueue()
                                    StatusModel.stepMessage = StepMessage(
                                        fromChessPiece.id,
                                        ChessPiecePosition(
                                            onClickChessPiece.position.x,
                                            onClickChessPiece.position.y,
                                            onClickChessPiece.position.biasY,
                                            onClickChessPiece.position.biasX,
                                            onClickChessPiece.id),
                                        Step.ROBOT)
                                }
                                else -> {
                                    StatusModel.putEvent(ChessPieceEvent(Model.getChessPieceById(view.id)))
                                }
                            }
                        } ?: run {
                            StatusModel.putEvent(ChessPieceEvent(Model.getChessPieceById(view.id)))
                        }

                        if (onClickChessPiece.group != Group.RED) return@run
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

            if (StatusModel.stepMessage.step == Step.ROBOT) return@setOnTouchListener v?.onTouchEvent(event) ?: false

            val displayMetrics = DisplayMetrics()
            this.windowManager.defaultDisplay.getMetrics(displayMetrics)
            StatusModel.getChessPieceEvent()?.let {
                val fromPosition = findViewById<ImageButton>(it.chessPiece.id)
                val targetPosition = Model.getNearPosition(event.x, event.y)
                if (Model.getChessPieceById(fromPosition.id).group != Group.RED) return@let
                when (Model.updateChessBoard(fromPosition.id, targetPosition)) {
                    ActionStatus.SUCCESS -> {
                        val targetPositionParams = fromPosition.layoutParams as ConstraintLayout.LayoutParams
                        Log.d("search position", "chessPieceEvent.verticalBias:" + targetPosition.verticalBias + "    chessPieceEvent.verticalBias: " + targetPosition.verticalBias)
                        targetPositionParams.verticalBias = targetPosition.verticalBias
                        targetPositionParams.horizontalBias = targetPosition.horizontalBias
                        fromPosition.layoutParams = targetPositionParams

                        Model.getChessPieceById(fromPosition.id).position.x = targetPosition.x
                        Model.getChessPieceById(fromPosition.id).position.y = targetPosition.y
                        Model.getChessPieceById(fromPosition.id).position.biasX = targetPosition.horizontalBias
                        Model.getChessPieceById(fromPosition.id).position.biasY = targetPosition.verticalBias
                        onClickMediaPlayer?.start()
                        StatusModel.clearQueue()
                        StatusModel.stepMessage = StepMessage(fromPosition.id, targetPosition, Step.ROBOT)
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
        RobotFactory.RobotEye(mHandler).execute()
    }

}