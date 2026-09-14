package com.example.myandroidapplication.ui.component

import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.repository.MockStockRepository
import com.example.myandroidapplication.ui.util.QuoteFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HoldingDiagnoseSheetTest {

    private val repo = MockStockRepository()

    @Test
    fun parsePositiveNumberRejectsInvalid() {
        assertNull(parsePositiveNumber(""))
        assertNull(parsePositiveNumber("0"))
        assertNull(parsePositiveNumber("-1"))
        assertNull(parsePositiveNumber("abc"))
        assertEquals(178.5, parsePositiveNumber("178.5"))
    }

    @Test
    fun diagnosisFollowsPriceAndAdvice() {
        val stock = repo.getStocks().first()
        val cost = stock.price * 1.10
        val qty = 100.0
        val result = diagnoseHolding(stock, cost, qty)
        assertNotNull(result)
        assertEquals("浮亏", result.statusLabel)
        assertEquals(cost, result.breakEven)
        assertEquals((stock.price - cost) * qty, result.pnl, 0.0001)
        assertTrue(result.strategy.contains(QuoteFormat.price(stock.price)))
        assertTrue(result.strategy.contains(stock.aiAdvice) || result.strategy.contains("支撑"))
    }

    @Test
    fun sellAdviceMentionsExitWhenProfitable() {
        val sell = repo.getStocks().first { it.aiAdvice == Stock.ADVICE_SELL }
        val cost = sell.price * 0.90
        val result = diagnoseHolding(sell, cost, 200.0)
        assertNotNull(result)
        assertEquals("浮盈", result.statusLabel)
        assertTrue(result.strategy.contains(Stock.ADVICE_SELL))
        assertTrue(result.strategy.contains(QuoteFormat.price(sell.resistance)))
    }
}
