package com.example.myandroidapplication.data.repository

import com.example.myandroidapplication.data.model.Stock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockStockRepositoryTest {

    private val repo = MockStockRepository()

    @Test
    fun returnsFiftyUniqueAShareStocks() {
        val stocks = repo.getStocks()
        assertEquals(50, stocks.size)
        assertEquals(50, stocks.map { it.symbol }.toSet().size)
        stocks.forEach { stock ->
            assertEquals(6, stock.symbol.length)
            assertTrue(stock.name.isNotBlank())
        }
    }

    @Test
    fun everyStockHasConsistentAiFields() {
        val stocks = repo.getStocks()
        val advice = setOf(Stock.ADVICE_BUY, Stock.ADVICE_HOLD, Stock.ADVICE_SELL)
        val risk = setOf(Stock.RISK_LOW, Stock.RISK_MID, Stock.RISK_HIGH)
        val alertTypes = setOf(Stock.ALERT_RISK, Stock.ALERT_FUND, Stock.ALERT_TREND)
        stocks.forEach { stock ->
            assertTrue(stock.aiConfidence in 0..100)
            assertTrue(stock.aiAdvice in advice)
            assertTrue(stock.riskLevel in risk)
            assertTrue(stock.signals.isNotEmpty())
            assertEquals(stock.signals.first(), stock.aiSignal)
            assertTrue(stock.aiInterpretation.isNotBlank())
            assertTrue(stock.adviceReason.isNotBlank())
            assertTrue(stock.aiSummary.contains("开盘"))
            assertTrue(stock.aiSummary.contains("涨跌幅"))
            assertTrue(stock.aiSummary.contains(stock.shortTrend))
            assertTrue(stock.alerts.size >= 3)
            assertEquals(alertTypes, stock.alerts.map { it.type }.toSet())
            assertTrue(stock.chartPoints.size >= 20)
            assertEquals(stock.price, stock.chartPoints.last().price)
            assertTrue(stock.buyPoint > 0.0)
            assertTrue(stock.sellPoint > stock.buyPoint || stock.aiAdvice == Stock.ADVICE_SELL)
        }
        assertTrue(stocks.any { it.aiAdvice == Stock.ADVICE_BUY })
        assertTrue(stocks.any { it.aiAdvice == Stock.ADVICE_HOLD })
        assertTrue(stocks.any { it.aiAdvice == Stock.ADVICE_SELL })
    }

    @Test
    fun adviceDoesNotContradictScenario() {
        repo.getStocks().forEach { stock ->
            when (stock.aiAdvice) {
                Stock.ADVICE_BUY -> {
                    assertTrue(stock.changePercent > 0.0, stock.symbol)
                    assertTrue(stock.ma5 >= stock.ma10, stock.symbol)
                    assertTrue(stock.price > stock.support, stock.symbol)
                    assertTrue(stock.riskLevel != Stock.RISK_HIGH, stock.symbol)
                }
                Stock.ADVICE_SELL -> {
                    assertTrue(stock.changePercent < 0.0, stock.symbol)
                    assertTrue(stock.ma5 <= stock.ma10, stock.symbol)
                    assertTrue(stock.price <= stock.support, stock.symbol)
                    assertEquals(Stock.RISK_HIGH, stock.riskLevel, stock.symbol)
                }
                Stock.ADVICE_HOLD -> {
                    assertEquals(Stock.RISK_MID, stock.riskLevel, stock.symbol)
                }
            }
        }
    }

    @Test
    fun getStockFindsBySymbol() {
        val listed = repo.getStocks().first()
        val found = repo.getStock(listed.symbol)
        assertNotNull(found)
        assertEquals(listed, found)
        assertEquals(null, repo.getStock("999999"))
    }
}
