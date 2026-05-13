package com.pokerhelper.core

import com.pokerhelper.core.evaluator.HandEvaluator
import com.pokerhelper.core.model.Card
import com.pokerhelper.core.model.HandCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandEvaluatorTest {

    private fun hand(vararg s: String): List<Card> = s.map { Card.fromString(it) }

    @Test
    fun `royal flush detected`() {
        val result = HandEvaluator.evaluate(hand("As", "Ks", "Qs", "Js", "Ts"))
        assertEquals(HandCategory.ROYAL_FLUSH, result.category)
    }

    @Test
    fun `straight flush detected`() {
        val result = HandEvaluator.evaluate(hand("9h", "8h", "7h", "6h", "5h"))
        assertEquals(HandCategory.STRAIGHT_FLUSH, result.category)
        assertEquals(9, result.tiebreakers[0])
    }

    @Test
    fun `four of a kind detected`() {
        val result = HandEvaluator.evaluate(hand("As", "Ah", "Ad", "Ac", "Kh"))
        assertEquals(HandCategory.FOUR_OF_A_KIND, result.category)
        assertEquals(listOf(14, 13), result.tiebreakers)
    }

    @Test
    fun `full house detected`() {
        val result = HandEvaluator.evaluate(hand("Ks", "Kh", "Kd", "Qc", "Qh"))
        assertEquals(HandCategory.FULL_HOUSE, result.category)
        assertEquals(listOf(13, 12), result.tiebreakers)
    }

    @Test
    fun `flush detected`() {
        val result = HandEvaluator.evaluate(hand("As", "Js", "9s", "5s", "2s"))
        assertEquals(HandCategory.FLUSH, result.category)
    }

    @Test
    fun `wheel straight A-2-3-4-5 detected with 5 as high card`() {
        val result = HandEvaluator.evaluate(hand("As", "2h", "3d", "4c", "5h"))
        assertEquals(HandCategory.STRAIGHT, result.category)
        assertEquals(5, result.tiebreakers[0])
    }

    @Test
    fun `regular straight detected`() {
        val result = HandEvaluator.evaluate(hand("9s", "8h", "7d", "6c", "5h"))
        assertEquals(HandCategory.STRAIGHT, result.category)
        assertEquals(9, result.tiebreakers[0])
    }

    @Test
    fun `three of a kind detected`() {
        val result = HandEvaluator.evaluate(hand("7s", "7h", "7d", "Kc", "2h"))
        assertEquals(HandCategory.THREE_OF_A_KIND, result.category)
    }

    @Test
    fun `two pair detected with correct tiebreakers`() {
        val result = HandEvaluator.evaluate(hand("As", "Ah", "Ks", "Kh", "2c"))
        assertEquals(HandCategory.TWO_PAIR, result.category)
        assertEquals(listOf(14, 13, 2), result.tiebreakers)
    }

    @Test
    fun `pair detected`() {
        val result = HandEvaluator.evaluate(hand("As", "Ah", "Ks", "Qh", "2c"))
        assertEquals(HandCategory.PAIR, result.category)
        assertEquals(listOf(14, 13, 12, 2), result.tiebreakers)
    }

    @Test
    fun `high card detected`() {
        val result = HandEvaluator.evaluate(hand("As", "Kh", "9d", "5c", "2h"))
        assertEquals(HandCategory.HIGH_CARD, result.category)
    }

    @Test
    fun `picks best 5 from 7 cards`() {
        // 7 карт: пара тузов в кармане + AAAJJ на борде = фулл-хаус AAA+JJ
        val result = HandEvaluator.evaluate(
            hand("As", "Ah", "Ad", "Jc", "Jh", "5d", "2c")
        )
        assertEquals(HandCategory.FULL_HOUSE, result.category)
        assertEquals(listOf(14, 11), result.tiebreakers)
    }

    @Test
    fun `flush beats straight`() {
        val straight = HandEvaluator.evaluate(hand("9s", "8h", "7d", "6c", "5h"))
        val flush = HandEvaluator.evaluate(hand("As", "Js", "9s", "5s", "2s"))
        assertTrue(flush > straight)
    }

    @Test
    fun `higher pair beats lower pair`() {
        val aces = HandEvaluator.evaluate(hand("As", "Ah", "9d", "5c", "2h"))
        val kings = HandEvaluator.evaluate(hand("Ks", "Kh", "9d", "5c", "2h"))
        assertTrue(aces > kings)
    }
}
