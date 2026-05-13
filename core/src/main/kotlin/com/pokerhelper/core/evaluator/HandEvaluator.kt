package com.pokerhelper.core.evaluator

import com.pokerhelper.core.model.Card
import com.pokerhelper.core.model.HandCategory
import com.pokerhelper.core.model.HandRank
import com.pokerhelper.core.model.Rank


object HandEvaluator {

    fun evaluate(cards: List<Card>): HandRank {
        require(cards.size in 5..7) { "Need 5-7 cards, got ${cards.size}" }
        require(cards.toSet().size == cards.size) { "Duplicate cards: $cards" }

        if (cards.size == 5) return evaluateFive(cards)

        // Перебираем все 5-карточные подмножества и берём лучшее
        return combinations(cards, 5)
            .map(::evaluateFive)
            .max()
    }

    private fun evaluateFive(cards: List<Card>): HandRank {
        require(cards.size == 5)

        val sorted = cards.sortedByDescending { it.rank.value }
        val rankCounts = sorted.groupingBy { it.rank }.eachCount()

        val groups = rankCounts.entries
            .sortedWith(compareByDescending<Map.Entry<Rank, Int>> { it.value }
                .thenByDescending { it.key.value })

        val isFlush = sorted.all { it.suit == sorted[0].suit }
        val straightHigh = detectStraight(sorted)

        // Проверки сверху вниз по силе
        when {
            isFlush && straightHigh == Rank.ACE.value ->
                return HandRank(HandCategory.ROYAL_FLUSH, listOf(14), sorted)
            isFlush && straightHigh != null ->
                return HandRank(HandCategory.STRAIGHT_FLUSH, listOf(straightHigh), sorted)
            groups[0].value == 4 -> {
                // Каре: [ранг_каре, кикер]
                val quadRank = groups[0].key.value
                val kicker = groups[1].key.value
                return HandRank(HandCategory.FOUR_OF_A_KIND, listOf(quadRank, kicker), sorted)
            }
            groups[0].value == 3 && groups[1].value == 2 -> {
                // Фулл-хаус: [тройка, пара]
                return HandRank(
                    HandCategory.FULL_HOUSE,
                    listOf(groups[0].key.value, groups[1].key.value),
                    sorted
                )
            }
            isFlush -> {
                // Флеш: все 5 рангов как тайбрейкер
                return HandRank(
                    HandCategory.FLUSH,
                    sorted.map { it.rank.value },
                    sorted
                )
            }
            straightHigh != null ->
                return HandRank(HandCategory.STRAIGHT, listOf(straightHigh), sorted)
            groups[0].value == 3 -> {
                // Тройка: [ранг_тройки, кикер1, кикер2]
                val tripsRank = groups[0].key.value
                val kickers = groups.drop(1).take(2).map { it.key.value }
                return HandRank(
                    HandCategory.THREE_OF_A_KIND,
                    listOf(tripsRank) + kickers,
                    sorted
                )
            }
            groups[0].value == 2 && groups[1].value == 2 -> {
                // Две пары: [старшая_пара, младшая_пара, кикер]
                val highPair = groups[0].key.value
                val lowPair = groups[1].key.value
                val kicker = groups[2].key.value
                return HandRank(
                    HandCategory.TWO_PAIR,
                    listOf(highPair, lowPair, kicker),
                    sorted
                )
            }
            groups[0].value == 2 -> {
                // Пара: [ранг_пары, кикер1, кикер2, кикер3]
                val pairRank = groups[0].key.value
                val kickers = groups.drop(1).take(3).map { it.key.value }
                return HandRank(
                    HandCategory.PAIR,
                    listOf(pairRank) + kickers,
                    sorted
                )
            }
            else -> {
                // Старшая карта: все 5 рангов
                return HandRank(
                    HandCategory.HIGH_CARD,
                    sorted.map { it.rank.value },
                    sorted
                )
            }
        }
    }

    private fun detectStraight(sorted: List<Card>): Int? {
        val values = sorted.map { it.rank.value }.distinct()
        if (values.size != 5) return null

        // Обычный стрит: 5 подряд идущих значений
        if (values[0] - values[4] == 4) return values[0]

        // Колесо A-2-3-4-5: значения будут [14, 5, 4, 3, 2]
        if (values == listOf(14, 5, 4, 3, 2)) return 5

        return null
    }

    private fun <T> combinations(list: List<T>, k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (list.size < k) return emptyList()
        val head = list[0]
        val tail = list.drop(1)
        val withHead = combinations(tail, k - 1).map { listOf(head) + it }
        val withoutHead = combinations(tail, k)
        return withHead + withoutHead
    }
}
