package com.pokerhelper.core.model

/** Исход сыгранной раздачи с точки зрения нашего решения. */
enum class HandOutcome { WON, FOLDED }

data class PlayedHand(
    val timestamp: Long,
    val holeCards: List<String>,
    val boardCards: List<String>,
    val outcome: HandOutcome,
    val equityPercent: Double?,
    val opponents: Int,
    val finalHandCategory: String?
)
