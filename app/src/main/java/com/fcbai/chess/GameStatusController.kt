package com.fcbai.chess

enum class GameStatusStep {
    EMPTY,
    CHESS_PIECE_ONCLIK,
    CHESS_BOARD_ONCLIK,
    MOVE,
    MOVE_COMPLETE

}

data class GameStatus(val gameStatusStep: GameStatusStep, val chessPiece: ChessPiece?, val position: Position?)
data class MoveInfo(val chessPiece: ChessPiece, val position: Position)

object GameStatusController {
    private val currentStatus: MutableList<GameStatus> = mutableListOf()

    fun updateStatus(gameStatusStep: GameStatusStep, position: Position) {
        if (currentStatus.isNotEmpty()) {
            val gameStatus = currentStatus.first().copy(position = position)
            currentStatus.clear()
            currentStatus.add(gameStatus)
        }
    }

    fun updateStatus(gameStatusStep: GameStatusStep, chessPiece: ChessPiece) {
        if (currentStatus.isNotEmpty()) {
            if (currentStatus.first().chessPiece?.name == chessPiece.name) {
                return
            }
            currentStatus.clear()
        }
        currentStatus.add(GameStatus(gameStatusStep, chessPiece, null))
    }

    fun getMoveInfo(): MoveInfo? {
        return currentStatus.first().chessPiece?.let { currentStatus.first().position?.let { it1 ->
            MoveInfo(it,
                it1
            )
        } }
    }
}