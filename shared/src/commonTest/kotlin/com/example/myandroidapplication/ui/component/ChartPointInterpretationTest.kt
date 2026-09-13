package com.example.myandroidapplication.ui.component

import com.example.myandroidapplication.data.repository.MockStockRepository
import com.example.myandroidapplication.ui.util.QuoteFormat
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ChartPointInterpretationTest {

    private val repo = MockStockRepository()

    @Test
    fun selectedPointTextContainsThatTimeAndPrice() {
        val stock = repo.getStocks().first()
        val index = 4
        val point = stock.chartPoints[index]
        val text = chartPointInterpretation(stock, index)
        assertTrue(text.contains(point.time))
        assertTrue(text.contains(QuoteFormat.price(point.price)))
        val other = chartPointInterpretation(stock, index + 3)
        assertNotEquals(text, other)
    }
}
