package com.fcbai.chess

import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class LayoutOnTouchListener(gameActivity: GameActivity): View.OnTouchListener {
    private val gameActivity = gameActivity
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (StatusModel.stepMessage.step == Step.AI) return v?.onTouchEvent(event) ?: false
        val displayMetrics = DisplayMetrics()
        gameActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        StatusModel.getChessPieceEvent()?.let {
            val fromView = gameActivity.findViewById<ImageButton>(it.chessPiece.id)
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
                    gameActivity.onClickMediaPlayer?.start()
                    StatusModel.clearQueue()
                    StatusModel.stepMessage = NotificationMessage(from!!, to, Step.AI)
                }
                else -> {
                    Toast.makeText(gameActivity, "不合法的位置", Toast.LENGTH_SHORT).show()
                }
            }

            val blinkView = StatusModel.peekBlinkView()
            blinkView?.clearAnimation()
            blinkView?.animation?.cancel()
        }
        return v?.onTouchEvent(event) ?: false
    }

}

class ButtonOnClickListener(gameActivity: GameActivity): View.OnClickListener {
    private val gameActivity = gameActivity
    override fun onClick(view: View) {
        if (StatusModel.stepMessage.step == Step.AI) return
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
                    val fromView = gameActivity.findViewById<ImageButton>(it.chessPiece.id)
                    view.visibility = View.INVISIBLE
                    Model.getChessPiece(view.id).isDeath = true
                    val fromLayout = fromView.layoutParams as ConstraintLayout.LayoutParams
                    fromLayout.verticalBias = toLayout.verticalBias
                    fromLayout.horizontalBias = toLayout.horizontalBias
                    fromView.layoutParams = fromLayout
                    gameActivity.onClickMediaPlayer?.start()

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

        if (onClickChessPiece.group != StatusModel.gameInfo.group) return
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