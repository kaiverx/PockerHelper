package com.pokerhelper.core.model

enum class Suit(val symbol: String) {
    CLUBS("♣"),
    DIAMONDS("♦"),
    HEARTS("♥"),
    SPADES("♠");

    companion object {
        /** Парсинг из символа: "c", "d", "h", "s" */
        fun fromChar(c: Char): Suit = when (c.lowercaseChar()) {
            'c' -> CLUBS
            'd' -> DIAMONDS
            'h' -> HEARTS
            's' -> SPADES
            else -> throw IllegalArgumentException("Unknown suit: $c")
        }
    }
}

enum class Rank(val value: Int, val symbol: String) {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "T"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A");

    companion object {
        fun fromChar(c: Char): Rank = when (c.uppercaseChar()) {
            '2' -> TWO
            '3' -> THREE
            '4' -> FOUR
            '5' -> FIVE
            '6' -> SIX
            '7' -> SEVEN
            '8' -> EIGHT
            '9' -> NINE
            'T' -> TEN
            'J' -> JACK
            'Q' -> QUEEN
            'K' -> KING
            'A' -> ACE
            else -> throw IllegalArgumentException("Unknown rank: $c")
        }
    }
}

data class Card(val rank: Rank, val suit: Suit) : Comparable<Card> {

    override fun toString(): String = "${rank.symbol}${suit.symbol}"

    override fun compareTo(other: Card): Int = rank.value - other.rank.value

    fun toShortString(): String = "${rank.symbol}${suit.name.first().lowercaseChar()}"

    companion object {
        fun fromString(s: String): Card {
            require(s.length == 2) { "Card string must be 2 chars: $s" }
            return Card(Rank.fromChar(s[0]), Suit.fromChar(s[1]))
        }
    }
}
