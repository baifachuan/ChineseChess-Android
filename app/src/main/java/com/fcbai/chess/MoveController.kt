package com.fcbai.chess

import java.math.BigDecimal
import kotlin.math.*

class MoveController {

    fun verifyStep(from: ChessPiece, mapping: Map<ChessPieceType, List<ChessPiece>>, to: Position): Boolean {

        val currentBiasX = from.position.biasX
        val currentBiasY = from.position.biasY
        val targetBiasX = to.biasX
        val targetBiasY = to.biasY

        val xMove = String.format("%.2f", abs(BigDecimal(targetBiasX.toString()).subtract(BigDecimal(currentBiasX.toString())).toFloat()).toDouble()).toFloat()
        val yMove = String.format("%.2f", abs(BigDecimal(targetBiasY.toString()).subtract(BigDecimal(currentBiasY.toString())).toFloat()).toDouble()).toFloat()

        val exist = Model.getChessPiece(to.biasX, to.biasY)
        exist?.let {
            if (it.group == from.group) {
                return false
            }
        }

        when (from.chessPieceType) {
            ChessPieceType.HORSE -> {
                var hasConstraint = 0
                if (String.format("%.2f", abs(BigDecimal(targetBiasX.toString()).subtract(BigDecimal(currentBiasX.toString())).toFloat()).toDouble()) == "0.25") {
                    val constraintPosition = if (targetBiasX > currentBiasX) { currentBiasX + 0.125 } else { currentBiasX - 0.125 }
                    hasConstraint = mapping.values
                        .map { f -> f.filter { f1 -> f1.position.biasY == currentBiasY && f1.position.biasX == constraintPosition.toFloat()} }.filter { f -> f.isNotEmpty() }
                        .count()
                }

                if (String.format("%.2f", abs(BigDecimal(targetBiasY.toString()).subtract(BigDecimal(currentBiasY.toString())).toFloat()).toDouble()) == "0.20") {
                    val constraintPosition = if (targetBiasY > currentBiasY) { currentBiasY + 0.1 } else { currentBiasY - 0.1 }
                    hasConstraint = mapping.values
                        .map { f -> f.filter { f1 -> f1.position.biasX == currentBiasX && f1.position.biasY == constraintPosition.toFloat()} }.filter { f -> f.isNotEmpty() }
                        .count()
                }
                return listOf("0.269", "0.236").contains(String.format("%.3f",
                    sqrt(abs(BigDecimal(targetBiasY.toString()).subtract(BigDecimal(currentBiasY.toString())).toFloat()).toDouble().pow(
                        2.0
                    ) + abs(targetBiasX - currentBiasX).toDouble().pow(2.0))
                )) && hasConstraint == 0
            }
            ChessPieceType.SOLDIER -> {

                return if (from.group == StatusModel.gameInfo.group) {
                    if (currentBiasY < 0.55) {
                        xMove <= 0.13 && yMove <= 0.101 && targetBiasY <= currentBiasY && (xMove == 0F || yMove == 0F)
                    } else {
                        xMove == 0.00F && yMove <= 0.101 && targetBiasY <= currentBiasY
                    }
                } else{
                    if (currentBiasY > 0.45) {
                        xMove <= 0.251 && yMove <= 0.101 && targetBiasY >= currentBiasY && (xMove == 0F || yMove == 0F)
                    } else {
                        xMove == 0.00F && yMove <= 0.101 && targetBiasY >= currentBiasY
                    }
                }
            }

            ChessPieceType.CAR -> {
                if (abs(targetBiasX - currentBiasX) > 0) {
                    return mapping.values.map { f ->
                        f.filter { f1 ->
                            !f1.isDeath && f1.position.biasX > min(
                                targetBiasX,
                                currentBiasX
                            ) && f1.position.biasX < max(targetBiasX, currentBiasX) && f1.position.biasY == currentBiasY
                        }
                    }.none { f -> f.isNotEmpty() }  && currentBiasY == targetBiasY
                } else {
                    return mapping.values.map { f ->
                        f.filter { f1 ->
                            !f1.isDeath && f1.position.biasY > min(
                                targetBiasY,
                                currentBiasY
                            ) && f1.position.biasY < max(targetBiasY, currentBiasY) && f1.position.biasX == currentBiasX
                        }
                    }.none { f -> f.isNotEmpty() }  && currentBiasX == targetBiasX
                }
            }

            ChessPieceType.CANNON -> {
                exist?.let {
                    if (abs(targetBiasX - currentBiasX) > 0) {
                        return mapping.values.flatMap { f ->
                            f.filter { f1 ->
                                !f1.isDeath && f1.position.biasX > min(
                                    targetBiasX,
                                    currentBiasX
                                ) && f1.position.biasX < max(targetBiasX, currentBiasX) && f1.position.biasY == currentBiasY
                            }
                        }.count() == 1 && currentBiasY == targetBiasY
                    } else {
                        return mapping.values.flatMap { f ->
                            f.filter { f1 ->
                                !f1.isDeath && f1.position.biasY > min(
                                    targetBiasY,
                                    currentBiasY
                                ) && f1.position.biasY < max(targetBiasY, currentBiasY) && f1.position.biasX == currentBiasX
                            }
                        }.count() == 1  && currentBiasX == targetBiasX
                    }
                } ?: run {
                    if (abs(targetBiasX - currentBiasX) > 0) {
                        return mapping.values.map { f ->
                            f.filter { f1 ->
                                !f1.isDeath && f1.position.biasX > min(
                                    targetBiasX,
                                    currentBiasX
                                ) && f1.position.biasX < max(targetBiasX, currentBiasX) && f1.position.biasY == currentBiasY
                            }
                        }.none { f -> f.isNotEmpty() }  && currentBiasY == targetBiasY
                    } else {
                        return mapping.values.map { f ->
                            f.filter { f1 ->
                                !f1.isDeath && f1.position.biasY > min(
                                    targetBiasY,
                                    currentBiasY
                                ) && f1.position.biasY < max(targetBiasY, currentBiasY) && f1.position.biasX == currentBiasX
                            }
                        }.none { f -> f.isNotEmpty() }  && currentBiasX == targetBiasX
                    }
                }
            }

            ChessPieceType.ELEPHANT -> {
                return mapping.values.flatMap { f ->
                    f.filter { f1 ->
                        !f1.isDeath && f1.position.biasX == min(
                            targetBiasX,
                            currentBiasX
                        ) + 0.125F && f1.position.biasY == min(targetBiasY, currentBiasY) + 0.1F
                    }
                }.isEmpty() && yMove == 0.20F && xMove == 0.250F
            }

            ChessPieceType.GENERAL -> {
                return if (from.group == StatusModel.gameInfo.group) {
                    val minX = 0.374F
                    val maxX = 0.6251F
                    val minY = 0.749F
                    val maxY = 0.951F
                    xMove <= 0.13 && yMove <= 0.101 && (xMove == 0F || yMove == 0F) && targetBiasX < maxX && minX < targetBiasX && targetBiasY < maxY && minY < targetBiasY
                } else {
                    val minX = 0.374F
                    val maxX = 0.6251F
                    val minY = 0.049F
                    val maxY = 0.251F
                    xMove <= 0.13 && yMove <= 0.101 &&  (xMove == 0F || yMove == 0F) && targetBiasX < maxX && minX < targetBiasX && targetBiasY < maxY && minY < targetBiasY

                }
            }

            ChessPieceType.SCHOLAR -> {
                val x = String.format("%.2f", abs(BigDecimal(xMove.toString()).subtract(BigDecimal(0.13F.toString())).toFloat()).toDouble())
                val y = String.format("%.2f", abs(BigDecimal(yMove.toString()).subtract(BigDecimal(0.1F.toString())).toFloat()).toDouble())
                return if (from.group == StatusModel.gameInfo.group) {
                    val minX = 0.374F
                    val maxX = 0.6251F
                    val minY = 0.749F
                    val maxY = 0.951F
                    x == "0.00" && y == "0.00" && targetBiasX < maxX && minX < targetBiasX && targetBiasY < maxY && minY < targetBiasY
                } else {
                    val minX = 0.374F
                    val maxX = 0.6251F
                    val minY = 0.049F
                    val maxY = 0.251F
                    x == "0.00" && y == "0.00" && targetBiasX < maxX && minX < targetBiasX && targetBiasY < maxY && minY < targetBiasY
                }
            }

            else -> {
                return false
            }
        }
    }
}
