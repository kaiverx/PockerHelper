package com.pokerhelper.core.equity

import com.pokerhelper.core.evaluator.HandEvaluator
import com.pokerhelper.core.model.Card
import com.pokerhelper.core.model.Deck

/**
 * Карта-аут с указанием качества и вероятности появления.
 *
 * @param card Сама карта.
 * @param winRate Доля симуляций, где эта карта приводит к победе (0..1).
 * @param probability Вероятность, что эта конкретная карта выпадет к риверу.
 */
data class Out(
    val card: Card,
    val winRate: Double,
    val probability: Double
) {
    fun winPercent(): String = "%.0f%%".format(winRate * 100)
    fun probabilityPercent(): String = "%.1f%%".format(probability * 100)
}

/**
 * Сводный результат по аутам.
 *
 * @param outs Сам список аутов, отсортированный по winRate убывая.
 * @param anyOutProbability Вероятность, что хотя бы один аут выпадет к риверу.
 */
data class OutsResult(
    val outs: List<Out>,
    val anyOutProbability: Double
) {
    fun anyOutPercent(): String = "%.1f%%".format(anyOutProbability * 100)
}

class OutsCalculator(
    private val iterationsPerCard: Int = 200
) {

    private fun thresholdFor(opponents: Int): Double =
        (0.75 - opponents * 0.05).coerceAtLeast(0.20)

    fun calculate(
        holeCards: List<Card>,
        board: List<Card>,
        opponents: Int = 1
    ): OutsResult {
        require(holeCards.size == 2) { "Hold'em hole cards must be 2" }
        require(opponents in 1..9) { "Opponents must be 1-9" }
        if (board.size !in 3..4) return OutsResult(emptyList(), 0.0)

        val known = (holeCards + board).toSet()
        val remainingCards = Deck.FULL_DECK.filterNot { it in known }
        val deckSize = remainingCards.size      // 47 на флопе, 46 на тёрне
        val streetsLeft = 5 - board.size         // 2 на флопе, 1 на тёрне
        val cardsToDealAfter = streetsLeft - 1   // ещё карт борда после "пробной"
        val threshold = thresholdFor(opponents)

        val outs = mutableListOf<Out>()

        for (candidateCard in remainingCards) {
            val knownAfter = known + candidateCard
            var wins = 0
            var ties = 0

            repeat(iterationsPerCard) {
                val deck = Deck(excluded = knownAfter).shuffle()
                // opponents*2 карт для всех противников + добор борда
                val drawn = deck.draw(opponents * 2 + cardsToDealAfter)
                val opponentHoles = drawn.take(opponents * 2).chunked(2)
                val extraBoard = drawn.drop(opponents * 2)

                val finalBoard = board + candidateCard + extraBoard
                val heroRank = HandEvaluator.evaluate(holeCards + finalBoard)
                // Берём ЛУЧШУЮ среди оппонентов: выигрываем, только если бьём всех
                val bestOppRank = opponentHoles
                    .map { HandEvaluator.evaluate(it + finalBoard) }
                    .max()

                when {
                    heroRank > bestOppRank -> wins++
                    heroRank == bestOppRank -> ties++
                }
            }

            val winRate = (wins + ties / 2.0) / iterationsPerCard
            if (winRate >= threshold) {
                // На тёрне: 1/46. На флопе: 1 − (45/47)*(44/46) — шанс хоть на одной из 2 улиц.
                val probability = when (streetsLeft) {
                    1 -> 1.0 / deckSize
                    2 -> 1.0 - ((deckSize - 1.0) / deckSize) * ((deckSize - 2.0) / (deckSize - 1.0))
                    else -> 0.0
                }
                outs.add(Out(candidateCard, winRate, probability))
            }
        }

        val sortedOuts = outs.sortedByDescending { it.winRate }
        val anyOut = computeAnyOutProbability(sortedOuts.size, deckSize, streetsLeft)
        return OutsResult(sortedOuts, anyOut)
    }

    /**
     * Вероятность хотя бы одного попадания: 1 − P(ни одна нужная не пришла).
     * Считается без возвращения карт.
     */
    private fun computeAnyOutProbability(outsCount: Int, deckSize: Int, streetsLeft: Int): Double {
        if (outsCount == 0 || streetsLeft <= 0) return 0.0
        val nonOuts = deckSize - outsCount
        var pNone = 1.0
        for (i in 0 until streetsLeft) {
            val available = deckSize - i
            val nonOutsLeft = nonOuts - i
            if (nonOutsLeft < 0) return 1.0
            pNone *= nonOutsLeft.toDouble() / available
        }
        return 1.0 - pNone
    }
}
