package com.pokerhelper.core.odds


data class PotOddsResult(
    val potOdds: Double,
    val requiredEquity: String,
    val recommendation: String
)

object PotOddsCalculator {

    fun calculate(
        potSize: Double,
        callAmount: Double,
        ourEquity: Double? = null
    ): PotOddsResult {
        require(potSize >= 0) { "Pot size must be non-negative" }
        require(callAmount > 0) { "Call amount must be positive" }

        val potOdds = callAmount / (potSize + callAmount)
        val requiredPercent = "%.1f%%".format(potOdds * 100)

        val recommendation = when {
            ourEquity == null ->
                "Нужно $requiredPercent equity для безубыточного колла"
            ourEquity > potOdds + 0.05 ->
                "CALL — equity ${"%.1f%%".format(ourEquity * 100)} заметно выше необходимых $requiredPercent"
            ourEquity > potOdds ->
                "Marginal CALL — equity чуть выше pot odds"
            else ->
                "FOLD — equity ${"%.1f%%".format(ourEquity * 100)} ниже требуемых $requiredPercent"
        }

        return PotOddsResult(potOdds, requiredPercent, recommendation)
    }
}
