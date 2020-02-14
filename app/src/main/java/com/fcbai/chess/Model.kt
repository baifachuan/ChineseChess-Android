package com.fcbai.chess

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.abs

data class Position(val biasX: Float = -1F, val biasY: Float = -1F,
                    val x: Float = -1F, val y: Float = -1F,
                    val horizontalPos: Int = 0, val verticalPos: Int = 0)

enum class Group {
    RED, BLACK
}
enum class ChessPieceType {
    SOLDIER, //兵
    HORSE, //马
    CANNON, //炮
    ELEPHANT, //相
    SCHOLAR, //仕
    GENERAL,  //将
    CAR //车
}

enum class ActionStatus {
    SUCCESS,
    CANNOTMOVE
}

enum class Step {
    AI,
    HUMAN,
    WATTING
}

data class ChessPiece(val name: String, var position: Position,
                      var group: Group, val id: Int,
                      val chessPieceType: ChessPieceType,
                      var isDeath: Boolean = false
)

open class Event(val timestamp: Long = System.currentTimeMillis(), var eventName: String)
data class ChessPieceEvent(val chessPiece: ChessPiece): Event(eventName = "ChessPieceEvent")

data class AbsolutePosition(val screenWith: Int, val screenHeight: Int,
                            val chessPieceWith: Int, val chessPieceHeight: Int,
                            val chessBoardWith: Int, val chessBoardHeight: Int
)
data class NetworkProtocol(val id: Int,
                           val current_x: Int,
                           val current_y: Int,
                           val target_x: Int,
                           val target_y: Int, val win: Int = -1, val action: Int = 0)

data class NetworkProtocol1(val id: String,
                           val current_x: String,
                           val current_y: String,
                           val target_x: String,
                           val target_y: String, val win: String = "", val action: String = "")

data class NotificationMessage(var from: Position = Position(),
                               var to: Position = Position(),
                               val step: Step = Step.HUMAN)

data class GameInfo(val id: Int = -1, val group: Group = Group.RED)

object StatusModel {
    var stepMessage: NotificationMessage = NotificationMessage()
    var gameInfo = GameInfo()
    var absolutePosition: AbsolutePosition = AbsolutePosition(-1, -1, -1, -1, -1, -1)

    private val blinkViewQueue = LinkedBlockingDeque<View>()

    private val eventQueue: MutableList<Event> = mutableListOf()

    fun peekBlinkView(): View? {
        val view = blinkViewQueue.peek()
        blinkViewQueue.remove(view)
        return view
    }

    fun putBlinkView(view: View) {
        blinkViewQueue.push(view)
    }

    fun putEvent(event: Event) {
        if (eventQueue.isNotEmpty()) eventQueue.clear()
        if (eventQueue.isEmpty() && event.eventName == "PositionEvent") {

        } else {
            eventQueue.add(event)
        }

    }

    fun clearQueue() {
        eventQueue.clear()
    }

    fun getChessPieceEvent(): ChessPieceEvent? {
        if (eventQueue.isEmpty()) return null
        return eventQueue.filter { f -> f.eventName == "ChessPieceEvent" }[0] as ChessPieceEvent
    }
}

object Model {


    private val soldiers: List<ChessPiece> = listOf(
        ChessPiece("soldier01", Position(0.00F, 0.35F, getXByBias(0.00F), getYByBias(0.35F)), Group.BLACK, R.id.soldier01, ChessPieceType.SOLDIER),
        ChessPiece("soldier02", Position(0.25F, 0.35F, getXByBias(0.25F), getYByBias(0.35F)), Group.BLACK, R.id.soldier02, ChessPieceType.SOLDIER),
        ChessPiece("soldier03", Position(0.50F, 0.35F, getXByBias(0.50F), getYByBias(0.35F)), Group.BLACK, R.id.soldier03, ChessPieceType.SOLDIER),
        ChessPiece("soldier04", Position(0.75F, 0.35F, getXByBias(0.75F), getYByBias(0.35F)), Group.BLACK, R.id.soldier04, ChessPieceType.SOLDIER),
        ChessPiece("soldier05", Position(1.00F, 0.35F, getXByBias(1.00F), getYByBias(0.35F)), Group.BLACK, R.id.soldier05, ChessPieceType.SOLDIER),
        ChessPiece("soldier06", Position(0.00F, 0.65F, getXByBias(0.00F), getYByBias(0.65F)), Group.RED, R.id.soldier06, ChessPieceType.SOLDIER),
        ChessPiece("soldier07", Position(0.25F, 0.65F, getXByBias(0.25F), getYByBias(0.65F)), Group.RED, R.id.soldier07, ChessPieceType.SOLDIER),
        ChessPiece("soldier08", Position(0.50F, 0.65F, getXByBias(0.50F), getYByBias(0.65F)), Group.RED, R.id.soldier08, ChessPieceType.SOLDIER),
        ChessPiece("soldier09", Position(0.75F, 0.65F, getXByBias(0.75F), getYByBias(0.65F)), Group.RED, R.id.soldier09, ChessPieceType.SOLDIER),
        ChessPiece("soldier10", Position(1.00F, 0.65F, getXByBias(1.00F), getYByBias(0.65F)), Group.RED, R.id.soldier10, ChessPieceType.SOLDIER)
    )


    private val cars: List<ChessPiece> = listOf(
        ChessPiece("car01", Position(0.00F, 0.05F, getXByBias(0.00F), getYByBias(0.05F)), Group.BLACK, R.id.car01, ChessPieceType.CAR),
        ChessPiece("car02", Position(1.00F, 0.05F, getXByBias(1.00F), getYByBias(0.05F)), Group.BLACK, R.id.car02, ChessPieceType.CAR),
        ChessPiece("car03", Position(0.00F, 0.95F, getXByBias(0.00F), getYByBias(0.95F)), Group.RED, R.id.car03, ChessPieceType.CAR),
        ChessPiece("car04", Position(1.00F, 0.95F, getXByBias(1.00F), getYByBias(0.95F)), Group.RED, R.id.car04, ChessPieceType.CAR)
    )

    private val horses: List<ChessPiece> = listOf(
        ChessPiece("horse01", Position(0.125F, 0.05F, getXByBias(0.125F), getYByBias(0.05F)), Group.BLACK, R.id.horse01, ChessPieceType.HORSE),
        ChessPiece("horse02", Position(0.875F, 0.05F, getXByBias(0.875F), getYByBias(0.05F)), Group.BLACK, R.id.horse02, ChessPieceType.HORSE),
        ChessPiece("horse03", Position(0.125F, 0.95F, getXByBias(0.125F), getYByBias(0.95F)), Group.RED, R.id.horse03, ChessPieceType.HORSE),
        ChessPiece("horse04", Position(0.875F, 0.95F, getXByBias(0.875F), getYByBias(0.95F)), Group.RED, R.id.horse04, ChessPieceType.HORSE)
    )

    private val elephants: List<ChessPiece> = listOf(
        ChessPiece("elephant01", Position(0.25F, 0.05F, getXByBias(0.25F), getYByBias(0.05F)), Group.BLACK, R.id.elephant01, ChessPieceType.ELEPHANT),
        ChessPiece("elephant02", Position(0.75F, 0.05F, getXByBias(0.75F), getYByBias(0.05F)), Group.BLACK, R.id.elephant02, ChessPieceType.ELEPHANT),
        ChessPiece("elephant03", Position(0.25F, 0.95F, getXByBias(0.25F), getYByBias(0.95F)), Group.RED, R.id.elephant03, ChessPieceType.ELEPHANT),
        ChessPiece("elephant04", Position(0.75F, 0.95F, getXByBias(0.75F), getYByBias(0.95F)), Group.RED, R.id.elephant04, ChessPieceType.ELEPHANT)
    )

    private val scholars: List<ChessPiece> = listOf(
        ChessPiece("scholar01", Position(0.375F, 0.05F, getXByBias(0.375F), getYByBias(0.05F)), Group.BLACK, R.id.scholar01, ChessPieceType.SCHOLAR),
        ChessPiece("scholar02", Position(0.625F, 0.05F, getXByBias(0.625F), getYByBias(0.05F)), Group.BLACK, R.id.scholar02, ChessPieceType.SCHOLAR),
        ChessPiece("scholar03", Position(0.375F, 0.95F, getXByBias(0.375F), getYByBias(0.95F)), Group.RED, R.id.scholar03, ChessPieceType.SCHOLAR),
        ChessPiece("scholar04", Position(0.625F, 0.95F, getXByBias(0.625F), getYByBias(0.95F)), Group.RED, R.id.scholar04, ChessPieceType.SCHOLAR)
    )

    private val cannons: List<ChessPiece> = listOf(
        ChessPiece("cannon01", Position(0.125F, 0.25F, getXByBias(0.125F), getYByBias(0.25F)), Group.BLACK, R.id.cannon01, ChessPieceType.CANNON),
        ChessPiece("cannon02", Position(0.875F, 0.25F, getXByBias(0.875F), getYByBias(0.25F)), Group.BLACK, R.id.cannon02, ChessPieceType.CANNON),
        ChessPiece("cannon03", Position(0.125F, 0.75F, getXByBias(0.125F), getYByBias(0.75F)), Group.RED, R.id.cannon03, ChessPieceType.CANNON),
        ChessPiece("cannon04", Position(0.875F, 0.75F, getXByBias(0.875F), getYByBias(0.75F)), Group.RED, R.id.cannon04, ChessPieceType.CANNON)
    )


    private val general: List<ChessPiece> = listOf(
        ChessPiece("general01", Position(0.5F, 0.05F, getXByBias(0.5F), getYByBias(0.05F)), Group.BLACK, R.id.general01, ChessPieceType.GENERAL),
        ChessPiece("general02", Position(0.5F, 0.95F, getXByBias(0.5F), getYByBias(0.95F)), Group.RED, R.id.general02, ChessPieceType.GENERAL)
    )

    private val redResources = mapOf(
        ChessPieceType.SOLDIER to R.drawable.hongzu,
        ChessPieceType.CAR to R.drawable.hongju,
        ChessPieceType.HORSE to R.drawable.hongma,
        ChessPieceType.CANNON to R.drawable.hongpao,
        ChessPieceType.SCHOLAR to R.drawable.hongshi,
        ChessPieceType.ELEPHANT to R.drawable.hongxiang,
        ChessPieceType.GENERAL to R.drawable.hongjiang
    )

    private val blackResources = mapOf(
        ChessPieceType.SOLDIER to R.drawable.heibing,
        ChessPieceType.CAR to R.drawable.heiju,
        ChessPieceType.HORSE to R.drawable.heima,
        ChessPieceType.CANNON to R.drawable.heipao,
        ChessPieceType.SCHOLAR to R.drawable.heishi,
        ChessPieceType.ELEPHANT to R.drawable.heixiang,
        ChessPieceType.GENERAL to R.drawable.heishuai
    )

    private val boardLayout = mapOf(
        ChessPieceType.SOLDIER to soldiers,
        ChessPieceType.CAR to cars,
        ChessPieceType.HORSE to horses,
        ChessPieceType.CANNON to cannons,
        ChessPieceType.SCHOLAR to scholars,
        ChessPieceType.ELEPHANT to elephants,
        ChessPieceType.GENERAL to general
    )

    private val chessBoard: MutableList<Position> = calculateBoard()

    private fun getXByBias(biasX: Float): Float {
        return biasX * StatusModel.absolutePosition.screenWith - 2 * biasX * StatusModel.absolutePosition.chessBoardWith / 2 + StatusModel.absolutePosition.chessBoardWith / 2
    }

    private fun getYByBias(biasY: Float): Float {
        return biasY * StatusModel.absolutePosition.screenHeight - 2 * biasY * StatusModel.absolutePosition.chessBoardHeight / 2 + StatusModel.absolutePosition.chessBoardHeight / 2
    }

    private fun calculateBoard(): MutableList<Position>  {
        val chessBoard: MutableList<Position> = mutableListOf()
        loop@ for (j in 0..9) {
            loop@ for (i in 0..8) {
                val biasY = (0.05F + 0.1 * j).toFloat()
                val biasX = 0.125F * i
                val x = biasX * StatusModel.absolutePosition.screenWith - 2 * biasX * StatusModel.absolutePosition.chessBoardWith / 2 + StatusModel.absolutePosition.chessBoardWith / 2
                val y = biasY * StatusModel.absolutePosition.screenHeight - 2 * biasY * StatusModel.absolutePosition.chessBoardHeight / 2 + StatusModel.absolutePosition.chessBoardHeight / 2
                chessBoard.add(Position(biasX, biasY, x, y, i, 9 - j))
            }
        }
        return chessBoard
    }

    fun getChessPiece(biasX: Float, biasY: Float): ChessPiece? {
        return getDefaultChessBoard().flatten().find { f -> f.position.biasX == biasX && f.position.biasY == biasY }
    }

    fun action2Notification(action: String): NotificationMessage {
        val posInfo = action.split("").filter { f -> f.isNotEmpty() }.map { f -> f.toInt() }
        val from = chessBoard.find { f -> f.horizontalPos == posInfo[0] && f.verticalPos == posInfo[1]}
        val to = chessBoard.find { f -> f.horizontalPos == posInfo[2] && f.verticalPos == posInfo[3]}
        return NotificationMessage(from = from!!, to = to!!)
    }

    fun notification2Action(notificationMessage: NotificationMessage): NetworkProtocol {
        val buff = StringBuffer()
        buff.append(notificationMessage.from.horizontalPos)
            .append(notificationMessage.from.verticalPos)
            .append(notificationMessage.to.horizontalPos)
            .append(notificationMessage.to.verticalPos)
        return NetworkProtocol(StatusModel.gameInfo.id, notificationMessage.from.horizontalPos, notificationMessage.from.verticalPos, notificationMessage.to.horizontalPos, notificationMessage.to.verticalPos)
    }

    fun getNearPosition(x: Float, y: Float): Position {
        return chessBoard.minBy { abs(it.x - x) + abs(it.y - y) }!!
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getResource(group: Group, chessPieceType: ChessPieceType): Int {
        return if (group != Group.RED)
            blackResources.getOrDefault(chessPieceType, R.drawable.hongzu)
        else
            redResources.getOrDefault(chessPieceType, R.drawable.hongzu)
    }
    fun getDefaultChessBoard(): List<List<ChessPiece>> {
        return listOf(soldiers, cars, horses, general, elephants, cannons, scholars)
    }

    fun getChessPiece(id: Int): ChessPiece {
        return getDefaultChessBoard().
            map { f -> f.filter { f1 -> f1.id == id } }.
            filter { f -> f.isNotEmpty() }[0][0]
    }

    fun getPosition(biasX: Float, biasY: Float): Position? {
        return chessBoard.find { f -> f.biasY == String.format("%.2f", biasY).toFloat() && f.biasX == biasX }
    }

    fun updateChessBoard(id: Int, to: Position): ActionStatus {
        val currentPosition = getChessPiece(id)
        return if (MoveController().verifyStep(currentPosition, boardLayout, to)) {
            Log.d("move success ", "move:" + currentPosition.chessPieceType.name)
            ActionStatus.SUCCESS
        } else {
            Log.d("move fail", "move:" + currentPosition.chessPieceType.name)
            ActionStatus.CANNOTMOVE
        }
    }
}