package com.pokerhelper.app.data

import com.pokerhelper.core.model.HandOutcome
import com.pokerhelper.core.model.PlayedHand

data class HandStats(
    val total: Int,
    val won: Int,
    val folded: Int,
    val winRate: Double?,
    val averageWinEquity: Double?
) {
    fun winRatePercent(): String =
        winRate?.let { "%.1f%%".format(it * 100) } ?: "—"

    fun averageWinEquityPercent(): String =
        averageWinEquity?.let { "%.1f%%".format(it) } ?: "—"

    companion object {
        fun from(hands: List<PlayedHand>): HandStats {
            val won = hands.count { it.outcome == HandOutcome.WON }
            val folded = hands.count { it.outcome == HandOutcome.FOLDED }
            val total = hands.size
            val winRate = if (total == 0) null else won.toDouble() / total
            val winEquities = hands
                .filter { it.outcome == HandOutcome.WON }
                .mapNotNull { it.equityPercent }
            val avgWinEq = if (winEquities.isEmpty()) null else winEquities.average()
            return HandStats(total, won, folded, winRate, avgWinEq)
        }
    }
}
