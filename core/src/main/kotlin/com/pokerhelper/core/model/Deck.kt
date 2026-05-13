package com.pokerhelper.core.model

import kotlin.random.Random

class Deck(excluded: Set<Card> = emptySet()) {

    private val cards: MutableList<Card> = FULL_DECK.filterNot { it in excluded }.toMutableList()

    /** Перемешать колоду. Передаём Random для тестируемости. */
    fun shuffle(random: Random = Random.Default): Deck {
        cards.shuffle(random)
        return this
    }

    /** Взять N карт сверху. Используется в Monte Carlo. */
    fun draw(count: Int): List<Card> {
        require(count <= cards.size) { "Not enough cards: need $count, have ${cards.size}" }
        val drawn = cards.takeLast(count)
        repeat(count) { cards.removeAt(cards.size - 1) }
        return drawn
    }

    fun remaining(): Int = cards.size

    companion object {
        val FULL_DECK: List<Card> = buildList {
            for (rank in Rank.values()) {
                for (suit in Suit.values()) {
                    add(Card(rank, suit))
                }
            }
        }
    }
}
