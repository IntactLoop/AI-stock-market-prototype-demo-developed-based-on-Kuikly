package com.example.myandroidapplication.ui.chat

import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.repository.MockStockRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatReplyPreferenceTest {

    private val stock = MockStockRepository().getStocks().first()

    @Test
    fun twoArgMatchesBalancedThreeArg() {
        val question = PresetQuestions.first()
        val two = answerQuestion(stock, question)
        val balanced = answerQuestion(stock, question, AppContainer.RISK_BALANCED)
        assertEquals(two.conclusion, balanced.conclusion)
        assertEquals(two.reason, balanced.reason)
        assertEquals(two.riskLevel, balanced.riskLevel)
    }

    @Test
    fun steadyWrapsWithoutChangingRiskOrDigits() {
        val question = PresetQuestions.first()
        val two = answerQuestion(stock, question)
        val wrapped = answerQuestion(stock, question, AppContainer.RISK_STEADY)
        assertEquals("稳健视角：" + two.conclusion, wrapped.conclusion)
        assertTrue(wrapped.reason.startsWith(two.reason))
        assertTrue(wrapped.reason.contains("仓位"))
        assertEquals(two.riskLevel, wrapped.riskLevel)
        assertFalse(wrapped.conclusion.contains("稳健视角：稳健视角："))
    }

    @Test
    fun aggressiveWrapsFollowUp() {
        val question = PresetQuestions[1]
        val previous = PresetQuestions.first()
        val two = answerFollowUp(stock, question, previous)
        val wrapped = answerFollowUp(stock, question, previous, AppContainer.RISK_AGGRESSIVE)
        assertEquals("进取视角：" + two.conclusion, wrapped.conclusion)
        assertTrue(wrapped.reason.contains("点位"))
        assertEquals(two.riskLevel, wrapped.riskLevel)
    }
}
