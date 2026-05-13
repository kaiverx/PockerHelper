package com.pokerhelper.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokerhelper.app.data.HandHistoryRepository
import com.pokerhelper.core.equity.EquityCalculator
import com.pokerhelper.core.equity.EquityResult
import com.pokerhelper.core.equity.OutsCalculator
import com.pokerhelper.core.equity.OutsResult
import com.pokerhelper.core.evaluator.HandEvaluator
import com.pokerhelper.core.model.Card
import com.pokerhelper.core.model.HandOutcome
import com.pokerhelper.core.model.HandRank
import com.pokerhelper.core.model.PlayedHand
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class CardSlot {
    data class Hole(val index: Int) : CardSlot()
    data class Board(val index: Int) : CardSlot()
}

enum class SavedMessage {
    WON_RECORDED,
    FOLD_RECORDED,
    NEED_HOLE_CARDS
}

data class HandAnalysisState(
    val holeCards: Map<Int, Card> = emptyMap(),
    val boardCards: Map<Int, Card> = emptyMap(),
    val selectedSlot: CardSlot? = null,
    val opponents: Int = 1,
    val handRank: HandRank? = null,
    val equity: EquityResult? = null,
    val outsResult: OutsResult? = null,
    val isCalculating: Boolean = false,
    val error: String? = null,
    val savedMessage: SavedMessage? = null
) {
    val holeCardsList: List<Card> get() = (0..1).mapNotNull { holeCards[it] }
    val boardCardsList: List<Card> get() = (0..4).mapNotNull { boardCards[it] }
    val allKnownCards: Set<Card> get() = (holeCards.values + boardCards.values).toSet()
}

class MainViewModel(
    private val historyRepository: HandHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HandAnalysisState())
    val state: StateFlow<HandAnalysisState> = _state.asStateFlow()

    private val equityCalculator = EquityCalculator()
    private val outsCalculator = OutsCalculator()
    private var calculationJob: Job? = null

    fun selectSlot(slot: CardSlot) {
        val current = _state.value
        val currentCard = current.cardAtSlot(slot)
        if (currentCard != null) {
            _state.update { it.clearSlot(slot).copy(selectedSlot = null) }
            recalculate()
        } else {
            _state.update { it.copy(selectedSlot = slot) }
        }
    }

    fun pickCard(card: Card) {
        val current = _state.value
        val existingSlot = current.slotOfCard(card)
        if (existingSlot != null) {
            _state.update { it.clearSlot(existingSlot).copy(selectedSlot = existingSlot) }
            recalculate()
            return
        }
        val targetSlot = current.selectedSlot ?: current.firstEmptySlot()
        if (targetSlot == null) return
        _state.update {
            it.setCardAtSlot(targetSlot, card).copy(selectedSlot = it.nextEmptySlotAfter(targetSlot))
        }
        recalculate()
    }

    fun setOpponents(count: Int) {
        _state.update { it.copy(opponents = count.coerceIn(1, 9)) }
        recalculate()
    }

    fun clear() {
        calculationJob?.cancel()
        _state.value = HandAnalysisState()
    }

    fun recordOutcome(outcome: HandOutcome) {
        val s = _state.value
        if (s.holeCardsList.size < 2) {
            _state.update { it.copy(savedMessage = SavedMessage.NEED_HOLE_CARDS) }
            return
        }
        val finalCategory = if (outcome == HandOutcome.WON) {
            s.handRank?.category?.displayName
        } else null

        val hand = PlayedHand(
            timestamp = System.currentTimeMillis(),
            holeCards = s.holeCardsList.map { it.toShortString() },
            boardCards = s.boardCardsList.map { it.toShortString() },
            outcome = outcome,
            equityPercent = s.equity?.equity?.let { it * 100 },
            opponents = s.opponents,
            finalHandCategory = finalCategory
        )
        historyRepository.save(hand)

        val msg = if (outcome == HandOutcome.WON) SavedMessage.WON_RECORDED else SavedMessage.FOLD_RECORDED
        calculationJob?.cancel()
        _state.value = HandAnalysisState(savedMessage = msg)
    }

    fun acknowledgeMessage() {
        _state.update { it.copy(savedMessage = null) }
    }

    private fun recalculate() {
        calculationJob?.cancel()
        val s = _state.value
        val holes = s.holeCardsList
        val board = s.boardCardsList

        if (holes.size < 2) {
            _state.update { it.copy(handRank = null, equity = null, outsResult = null) }
            return
        }
        val boardValid = board.size in listOf(0, 3, 4, 5)
        if (!boardValid) {
            _state.update { it.copy(handRank = null, equity = null, outsResult = null) }
            return
        }

        calculationJob = viewModelScope.launch {
            _state.update { it.copy(isCalculating = true, error = null) }
            try {
                val allKnown = holes + board
                val rank = if (allKnown.size >= 5) {
                    withContext(Dispatchers.Default) { HandEvaluator.evaluate(allKnown) }
                } else null

                val equity = withContext(Dispatchers.Default) {
                    equityCalculator.calculate(holes, board, s.opponents, iterations = 5_000)
                }

                val outsResult = if (board.size in 3..4) {
                    withContext(Dispatchers.Default) {
                        outsCalculator.calculate(holes, board, s.opponents)
                    }
                } else null

                _state.update {
                    it.copy(
                        handRank = rank,
                        equity = equity,
                        outsResult = outsResult,
                        isCalculating = false
                    )
                }
            } catch (e: CancellationException) {
                // Штатная отмена корутины при изменении карт/оппонентов.
                // Пробрасываем, чтобы coroutine machinery корректно завершила Job.
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isCalculating = false, error = e.message ?: "Error") }
            }
        }
    }
}

private fun HandAnalysisState.cardAtSlot(slot: CardSlot): Card? = when (slot) {
    is CardSlot.Hole -> holeCards[slot.index]
    is CardSlot.Board -> boardCards[slot.index]
}

private fun HandAnalysisState.clearSlot(slot: CardSlot): HandAnalysisState = when (slot) {
    is CardSlot.Hole -> copy(holeCards = holeCards - slot.index)
    is CardSlot.Board -> copy(boardCards = boardCards - slot.index)
}

private fun HandAnalysisState.setCardAtSlot(slot: CardSlot, card: Card): HandAnalysisState = when (slot) {
    is CardSlot.Hole -> copy(holeCards = holeCards + (slot.index to card))
    is CardSlot.Board -> copy(boardCards = boardCards + (slot.index to card))
}

private fun HandAnalysisState.slotOfCard(card: Card): CardSlot? {
    holeCards.entries.find { it.value == card }?.let { return CardSlot.Hole(it.key) }
    boardCards.entries.find { it.value == card }?.let { return CardSlot.Board(it.key) }
    return null
}

private fun HandAnalysisState.firstEmptySlot(): CardSlot? {
    for (i in 0..1) if (i !in holeCards) return CardSlot.Hole(i)
    for (i in 0..4) if (i !in boardCards) return CardSlot.Board(i)
    return null
}

private fun HandAnalysisState.nextEmptySlotAfter(slot: CardSlot): CardSlot? {
    val order = (0..1).map { CardSlot.Hole(it) as CardSlot } +
                (0..4).map { CardSlot.Board(it) as CardSlot }
    val idx = order.indexOf(slot)
    return order.drop(idx + 1).firstOrNull { cardAtSlot(it) == null }
}
