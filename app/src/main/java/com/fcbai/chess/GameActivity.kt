package com.fcbai.chess

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout


class GameActivity: AppCompatActivity() {

    var onClickMediaPlayer: MediaPlayer? = null

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
                val imageButton = findViewById<ImageButton>(f.id)
                imageButton.background = BitmapDrawable(
                    resources,
                    combineImage(BitmapFactory.decodeResource(
                        resources, R.drawable.qizi), BitmapFactory.decodeResource(
                        resources, Model.getResource(f.group, f.chessPieceType))
                    )
                )
                val params = imageButton.layoutParams as ConstraintLayout.LayoutParams
//                Log.d("position", "x: " + f.position.x + "  Y: " + f.position.y)
//                Log.d("imageButton", "w: " + imageButton.width + "  h: " + imageButton.height)
                params.horizontalBias = f.position.biasX
                params.verticalBias = f.position.biasY
                imageButton.layoutParams = params
                imageButton.setOnClickListener { view ->
                    run {
                        val params1 = view.layoutParams as ConstraintLayout.LayoutParams
//                        Log.d("view.x", view.x.toString())
//                        Log.d("view.y", view.y.toString())
//                        Log.d("layout.biasX", params1.horizontalBias.toString())
//                        Log.d("layout.biasY", params1.verticalBias.toString())

                        val blinkView = StatusModel.peekBlinkView()
                        blinkView?.let {
                            blinkView.clearAnimation()
                            blinkView.animation?.cancel()
                            if (blinkView.id != view.id) {
                                view.startAnimation(ViewHelper.getAlphaAnimationForBlink())
                            }
                        } ?: run {
                            view.startAnimation(ViewHelper.getAlphaAnimationForBlink())
                        }
                        StatusModel.putBlinkView(view)
                        StatusModel.putEvent(ChessPieceEvent(Model.getChessPieceById(view.id)))

                        //这里需要检查，吃掉对方棋子的情况
                        if (StatusModel.isOk(params1.horizontalBias, params1.verticalBias)) {
                            val fromChessPiece = findViewById<ImageButton>(StatusModel.getChessPieceEvent().chessPiece.id)

                            if (fromChessPiece != null) {
                                Log.d("existChessPiece", "Found")
                            } else {
                                Log.d("existChessPiece", "Not Found")
                            }

                            view.visibility = View.INVISIBLE

                            val params02 = fromChessPiece.layoutParams as ConstraintLayout.LayoutParams
                            params02.verticalBias = params1.verticalBias
                            params02.horizontalBias = params1.horizontalBias
                            fromChessPiece.layoutParams = params02

                            onClickMediaPlayer?.start()

                            Model.getChessPieceById(fromChessPiece.id).position.x = Model.getChessPieceById(view.id).position.x
                            Model.getChessPieceById(fromChessPiece.id).position.y = Model.getChessPieceById(view.id).position.y

                            val blinkView = StatusModel.peekBlinkView()
                            blinkView?.clearAnimation()
                            blinkView?.animation?.cancel()
                        }
                    }
                }

            }
        }


        constraintLayout.setOnTouchListener { v, event ->
            StatusModel.putEvent(PositionEvent(Position(event.x, event.y)))
            val displayMetrics = DisplayMetrics()
            this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
            val qipanWidth = displayMetrics.widthPixels - 20
            val qipanHeight = displayMetrics.heightPixels / 3 * 2

//            Log.d("pingmu", "displayMetrics.widthPixels:" + displayMetrics.widthPixels + "    displayMetrics.heightPixels:"  + displayMetrics.heightPixels)
//            Log.d("qipan", "qipanWidth:" + qipanWidth + "    qipanHeight:"  + qipanHeight)
//            Log.d("constraintLayout", "constraintLayout.w: " + constraintLayout.width + "  constraintLayout.h:" + constraintLayout.height)
//            Log.d("qizi", "w: " + v.width + "  h:" + v.height)


            if (StatusModel.isOk()) {
                val imageButton = findViewById<ImageButton>(StatusModel.getChessPieceEvent().chessPiece.id)
                val chessPieceEvent = Model.getNearPosition(event.x, event.y)


                when (Model.updateChessBoard(imageButton.id, chessPieceEvent)) {
                    ActionStatus.SUCCESS -> {
                        val params = imageButton.layoutParams as ConstraintLayout.LayoutParams

                        Log.d("search position", "chessPieceEvent.verticalBias:" + chessPieceEvent.verticalBias + "    chessPieceEvent.verticalBias: " + chessPieceEvent.verticalBias)
                        val existChessPiece = Model.getChessPieceByPosition(chessPieceEvent.verticalBias, chessPieceEvent.horizontalBias)
                        if (existChessPiece != null) {
                            existChessPiece.isDeath = true
                            findViewById<ImageButton>(existChessPiece.id).visibility = View.INVISIBLE
                            Log.d("existChessPiece", "biasX:" + existChessPiece.position.biasX + "    biasY: " + existChessPiece.position.biasY)
                        } else {
                            Log.d("existChessPiece", "Not Found")
                        }


                        params.verticalBias = chessPieceEvent.verticalBias
                        params.horizontalBias = chessPieceEvent.horizontalBias
                        imageButton.layoutParams = params

                        Model.getChessPieceById(imageButton.id).position.x = chessPieceEvent.x
                        Model.getChessPieceById(imageButton.id).position.y = chessPieceEvent.y
                        Model.getChessPieceById(imageButton.id).position.biasX = chessPieceEvent.horizontalBias
                        Model.getChessPieceById(imageButton.id).position.biasY = chessPieceEvent.verticalBias
                        onClickMediaPlayer?.start()
                    }
                }

                val blinkView = StatusModel.peekBlinkView()
                blinkView?.clearAnimation()
                blinkView?.animation?.cancel()

//            val anim = TranslateAnimation(0F, event.biasX - imageButton.biasX, 0F, event.biasY - imageButton.biasY)
//            anim.fillAfter = true
//            anim.duration = 2 * 1000
//            imageButton.startAnimation(anim)
            }


            v?.onTouchEvent(event) ?: false
        }

    }


    private fun combineImage(original: Bitmap, overlay: Bitmap, isRotate: Boolean = false): Bitmap {
        val result = Bitmap.createBitmap(
            original.width, original
                .height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        val paint = Paint()
        paint.isAntiAlias = true
        canvas.drawBitmap(
            original,
            ((canvas.width - original.width)/2).toFloat(),
            (canvas.height - original.height)/2.toFloat(),
            paint)

        val m = Matrix()
        m.postScale(1F, -1F)

        canvas.drawBitmap(
            if(isRotate)  convert(overlay) else overlay,
            ((canvas.width - overlay.width)/2).toFloat(),
            (canvas.height - overlay.height)/2.toFloat(),
            paint)
        return result
    }


    private fun convert(a: Bitmap): Bitmap {
        val w = a.width
        val h = a.height
        val newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)// 创建一个新的和SRC长度宽度一样的位图
        val cv = Canvas(newb)
        val m = Matrix()
        m.postScale(1F, -1F)
        val new2 = Bitmap.createBitmap(a, 0, 0, w, h, m, true)
        cv.drawBitmap(new2, Rect(0, 0, new2.width, new2.height), Rect(0, 0, w, h), null)
        return newb
    }
}