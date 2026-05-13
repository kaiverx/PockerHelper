package com.pokerhelper.core

import com.pokerhelper.core.equity.OutsCalculator
import com.pokerhelper.core.model.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutsCalculatorTest {

    private fun cards(vararg s: String): List<Card> = s.map { Card.fromString(it) }

    @Test
    fun `no outs on preflop`() {
        val calc = OutsCalculator()
        assertEquals(0, calc.calculate(cards("As", "Ks"), emptyList()).outs.size)
    }

    @Test
    fun `no outs on river`() {
        val calc = OutsCalculator()
        assertEquals(0, calc.calculate(
            cards("As", "Ks"),
            cards("Qh", "Jd", "9c", "4s", "2h")
        ).outs.size)
    }

    @Test
    fun `weak hand vs many opponents has few outs`() {
        // 7-2 offsuit — мусорная рука против 9 оппонентов
        // почти никакая карта не даст победу в 30%+ случаев
        val calc = OutsCalculator(iterationsPerCard = 100)
        val result = calc.calculate(
            holeCards = cards("7h", "2c"),
            board = cards("Ks", "Qd", "Jh", "9s"),
            opponents = 9
        )
        assertTrue(
            "Trash hand vs 9 opponents should have very few outs, got ${result.outs.size}",
            result.outs.size < 10
        )
    }

    @Test
    fun `flush draw has outs`() {
        // 4 червы в кармане + флоп, нужна пятая черва
        val calc = OutsCalculator(iterationsPerCard = 100)
        val result = calc.calculate(
            holeCards = cards("Ah", "Kh"),
            board = cards("Qh", "7h", "2s"),
            opponents = 1
        )
        // Флеш-дро против 1 оппонента — должны быть ауты
        assertTrue(
            "Flush draw should have outs, got ${result.outs.size}",
            result.outs.size > 0
        )
    }

    @Test
    fun `any out probability is between 0 and 1`() {
        val calc = OutsCalculator(iterationsPerCard = 100)
        val result = calc.calculate(
            holeCards = cards("Ah", "Kh"),
            board = cards("Qh", "7h", "2s"),
            opponents = 1
        )
        assertTrue(result.anyOutProbability in 0.0..1.0)
    }

    @Test
    fun `no outs when board size is not flop or turn`() {
        val calc = OutsCalculator()
        // Префлоп
        assertEquals(0, calc.calculate(cards("As", "Ks"), emptyList()).outs.size)
        // Борд из 2 карт — нет смысла (не существует в реальной игре)
        assertEquals(0, calc.calculate(cards("As", "Ks"), cards("Qh", "Jd")).outs.size)
    }
}