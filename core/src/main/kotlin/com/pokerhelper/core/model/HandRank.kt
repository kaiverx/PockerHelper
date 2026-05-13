package com.pokerhelper.core.model

enum class HandCategory(val strength: Int, val displayName: String) {
    HIGH_CARD(1, "Старшая карта"),
    PAIR(2, "Пара"),
    TWO_PAIR(3, "Две пары"),
    THREE_OF_A_KIND(4, "Сет / Тройка"),
    STRAIGHT(5, "Стрит"),
    FLUSH(6, "Флеш"),
    FULL_HOUSE(7, "Фулл-хаус"),
    FOUR_OF_A_KIND(8, "Каре"),
    STRAIGHT_FLUSH(9, "Стрит-флеш"),
    ROYAL_FLUSH(10, "Роял-флеш")
}

data class HandRank(
    val category: HandCategory,
    val tiebreakers: List<Int>,
    val cards: List<Card>
) : Comparable<HandRank> {

    override fun compareTo(other: HandRank): Int {
        val categoryDiff = category.strength - other.category.strength
        if (categoryDiff != 0) return categoryDiff
        // Та же категория — сравниваем тайбрейкеры по очереди
        for (i in tiebreakers.indices) {
            if (i >= other.tiebreakers.size) return 1
            val diff = tiebreakers[i] - other.tiebreakers[i]
            if (diff != 0) return diff
        }
        return 0
    }

    override fun toString(): String = "${category.displayName} (${cards.joinToString(" ")})"
}
