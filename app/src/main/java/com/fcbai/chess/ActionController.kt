package com.fcbai.chess

import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class ActionController {
    fun isOk(chessPiece: ChessPiece, mapping: Map<ChessPieceType, List<ChessPiece>>, targetPosition: ChessPiecePosition): Boolean {
        when (chessPiece.chessPieceType) {
            ChessPieceType.HORSE -> {
                val currentBiasX = chessPiece.position.biasX
                val currentBiasY = chessPiece.position.biasY
                val targetBiasX = targetPosition.horizontalBias
                val targetBiasY = targetPosition.verticalBias
                var hasConstraint = 0
                if (abs(BigDecimal(targetBiasX.toString()).subtract(BigDecimal(currentBiasX.toString())).toFloat()).toDouble() == 0.125 * 2) {
                    val constraintPosition = if (targetBiasY > currentBiasY) { currentBiasY + 0.125 } else { currentBiasX - 0.125 }
                    hasConstraint = mapping.values
                        .map { f -> f.filter { f1 -> f1.position.biasY == currentBiasY && f1.position.biasX == constraintPosition.toFloat()} }.filter { f -> f.isNotEmpty() }
                        .count()
                }

                if (abs(BigDecimal(targetBiasY.toString()).subtract(BigDecimal(currentBiasY.toString())).toFloat()).toDouble() == 0.2 * 2) {
                    val constraintPosition = if (targetBiasY > currentBiasY) { currentBiasY + 0.1 } else { currentBiasY - 0.1 }
                    hasConstraint = mapping.values
                        .map { f -> f.filter { f1 -> f1.position.biasX == currentBiasX && f1.position.biasY == constraintPosition.toFloat()} }.filter { f -> f.isNotEmpty() }
                        .count()
                }

                listOf("0.269", "0.236").contains(String.format("%.3f",
                    sqrt(abs(BigDecimal(targetBiasY.toString()).subtract(BigDecimal(currentBiasY.toString())).toFloat()).toDouble().pow(
                        2.0
                    ) + abs(targetBiasX - currentBiasX).toDouble().pow(2.0))
                ))
//                return abs(BigDecimal(targetBiasY.toString()).subtract(BigDecimal(currentBiasY.toString())).toFloat()) == 0.2F
//                        && abs(targetBiasX - currentBiasX) == 0.125F
//                        && hasConstraint == 0
                return listOf("0.269", "0.236").contains(String.format("%.3f",
                    sqrt(abs(BigDecimal(targetBiasY.toString()).subtract(BigDecimal(currentBiasY.toString())).toFloat()).toDouble().pow(
                        2.0
                    ) + abs(targetBiasX - currentBiasX).toDouble().pow(2.0))
                )) && hasConstraint == 0
            }
            else -> {
                return false
            }
        }
    }
}

class SoldierController() {

}