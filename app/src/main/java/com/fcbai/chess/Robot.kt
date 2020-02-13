//package com.fcbai.chess
//
//import android.os.AsyncTask
//import android.os.Handler
//import android.os.Message
//import android.util.Log
//import com.beust.klaxon.Klaxon
//import com.github.kittinunf.fuel.Fuel
//
//interface Robot {
//    fun run(id: Int, targetPosition: ChessPiecePosition)
//}
//
//class RuleRobot(mHandler: Handler): Robot {
//    private val mHandler = mHandler
//
//    override fun run(id: Int, targetPosition: ChessPiecePosition) {
//        val myChessPieces = Model.getDefaultChessBoard().flatMap { f -> f.filter { f1 -> !f1.isDeath && f1.group == Group.BLACK } }
//        val soldier05 = myChessPieces.first { f -> f.name == "soldier05" }
//        val chessPiecePosition = Model.getChessPieceById(soldier05.position.biasX, soldier05.position.biasY + 0.1F)
//        chessPiecePosition?.let {
//
//           val from =  ChessPiecePosition(
//                soldier05.position.x,
//                soldier05.position.y,
//                soldier05.position.biasY,
//                soldier05.position.biasX,
//                soldier05.id)
//
//            soldier05.position.x = it.x
//            soldier05.position.y = it.y
//            soldier05.position.biasX = it.horizontalBias
//            soldier05.position.biasY = it.verticalBias
//
//            val notificationMessage = NotificationMessage(-1, from, it)
//
//            val msg = Message()
//            msg.what = GameActivity.AI_UPDATE
//            msg.obj = Klaxon().toJsonString(notificationMessage)
//            mHandler.sendMessage(msg)
//        }
//    }
//
//}
//
//
//object RobotFactory {
//
//
//
//
//
//    class RobotEye(mHandler: Handler): AsyncTask<Void, Void, String>() {
//        val mHandler = mHandler
//        override fun doInBackground(vararg params: Void?): String? {
//            while (true) {
//
//                val bodyJson = """{
//                    "id": 1,
//                    "current_x": 0,
//                    "current_y": 0,
//                    "target_x": 0,
//                    "target_y": 1
//                }"""
////                val (request, response, result) = Fuel.post("http://10.0.2.2:5000/start")
////                    .body(bodyJson)
////                    .response()
//                NetworkHelper.receive()
//                NetworkHelper.send(bodyJson)
//
//                if (StatusModel.stepMessage.step == Step.ROBOT) {
//                    Log.d("ROBOT", "is robot")
//                    RuleRobot(mHandler).run(StatusModel.stepMessage.id, StatusModel.stepMessage.targetPosition)
//                    StatusModel.stepMessage = StatusModel.stepMessage.copy(step = Step.HUMAN)
//                } else {
//                    Log.d("HUMAN", "is human")
//                }
//                Thread.sleep(3 * 1000)
//            }
//            return null
//        }
//    }
//}