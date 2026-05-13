package com.pokerhelper.core.equity

import com.pokerhelper.core.evaluator.HandEvaluator
import com.pokerhelper.core.model.Card
import com.pokerhelper.core.model.Deck
import kotlin.random.Random

data class EquityResult(
    val winRate: Double,
    val tieRate: Double,
    val equity: Double,
    val iterations: Int
) {
    fun winPercent(): String = "%.1f%%".format(winRate * 100)
    fun equityPercent(): String = "%.1f%%".format(equity * 100)
}

class EquityCalculator(private val random: Random = Random.Default) {

    fun calculate(
        holeCards: List<Card>,
        board: List<Card> = emptyList(),
        opponents: Int = 1,
        iterations: Int = 10_000
    ): EquityResult {
        require(holeCards.size == 2) { "Hold'em hole cards must be 2" }
        require(board.size in listOf(0, 3, 4, 5)) { "Board must be 0, 3, 4, or 5 cards" }
        require(opponents in 1..9) { "Opponents must be 1-9" }

        val known = (holeCards + board).toSet()
        require(known.size == holeCards.size + board.size) { "Duplicate cards in input" }

        val cardsToDraw = (5 - board.size) + opponents * 2

        var wins = 0
        var ties = 0

        repeat(iterations) {
            // Свежая колода без известных карт, перемешанная
            val deck = Deck(excluded = known).shuffle(random)
            val drawn = deck.draw(cardsToDraw)

            val completedBoard = board + drawn.take(5 - board.size)
            val opponentHoles = drawn.drop(5 - board.size).chunked(2)

            val heroRank = HandEvaluator.evaluate(holeCards + completedBoard)
            val oppRanks = opponentHoles.map { HandEvaluator.evaluate(it + completedBoard) }

            val bestOpp = oppRanks.max()
            when {
                heroRank > bestOpp -> wins++
                heroRank == bestOpp -> ties++
                // иначе проигрыш — ничего не считаем
            }
        }

        val winRate = wins.toDouble() / iterations
        val tieRate = ties.toDouble() / iterations
        val equity = winRate + tieRate / 2.0

        return EquityResult(winRate, tieRate, equity, iterations)
    }
}
