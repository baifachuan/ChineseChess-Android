package com.fcbai.chess

import android.os.AsyncTask
import android.os.Handler
import android.util.Log

interface Robot {
    fun run(id: Int, targetPosition: ChessPiecePosition)
}

class RuleRobot(mHandler: Handler): Robot {
    private val mHandler = mHandler

    override fun run(id: Int, targetPosition: ChessPiecePosition) {
        val myChessPieces = Model.getDefaultChessBoard().flatMap { f -> f.filter { f1 -> !f1.isDeath && f1.group == Group.BLACK } }
        val soldier05 = myChessPieces.first { f -> f.name == "soldier05" }
        val chessPiecePosition = Model.getChessPieceById(soldier05.position.biasX, soldier05.position.biasY + 0.1F)
        chessPiecePosition?.let {
            soldier05.position.x = it.x
            soldier05.position.y = it.y
            soldier05.position.biasX = it.horizontalBias
            soldier05.position.biasY = it.verticalBias
            StatusModel.robotStepMessage = RobotStepMessage(soldier05.id, it)
            mHandler.sendEmptyMessage(GameActivity.ROBOT_UPDATE)
        }
    }

}


object RobotFactory {

    class RobotEye(mHandler: Handler): AsyncTask<Void, Void, String>() {
        val mHandler = mHandler
        override fun doInBackground(vararg params: Void?): String? {
            while (true) {
                if (StatusModel.stepMessage.step == Step.ROBOT) {
                    Log.d("ROBOT", "is robot")
                    RuleRobot(mHandler).run(StatusModel.stepMessage.id, StatusModel.stepMessage.targetPosition)
                    StatusModel.stepMessage = StatusModel.stepMessage.copy(step = Step.HUMAN)
                } else {
                    Log.d("HUMAN", "is human")
                }
                Thread.sleep(3 * 1000)
            }
            return null
        }
    }
}