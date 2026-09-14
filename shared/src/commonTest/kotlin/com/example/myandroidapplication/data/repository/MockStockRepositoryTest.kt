package com.example.myandroidapplication.data.repository

import com.example.myandroidapplication.data.model.AdjustType
import com.example.myandroidapplication.data.model.ChainPeer
import com.example.myandroidapplication.data.model.ChartPeriod
import com.example.myandroidapplication.data.model.ChartType
import com.example.myandroidapplication.data.model.ETF
import com.example.myandroidapplication.data.model.RankingType
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.model.StockFilter
import com.example.myandroidapplication.data.model.StockPickCategory
import com.example.myandroidapplication.ui.util.QuoteFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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
            assertEquals(Stock.MARKET_CN, stock.market)
        }
    }

    @Test
    fun everyStockHasConsistentAiFields() {
        val stocks = repo.getStocks()
        val advice = setOf(Stock.ADVICE_BUY, Stock.ADVICE_HOLD, Stock.ADVICE_SELL)
        val risk = setOf(Stock.RISK_LOW, Stock.RISK_MID, Stock.RISK_HIGH)
        val alertTypes = setOf(
            Stock.ALERT_RISK,
            Stock.ALERT_FUND,
            Stock.ALERT_TREND,
            Stock.ALERT_EVENT
        )
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
            assertTrue(stock.alerts.size >= 4)
            assertEquals(alertTypes, stock.alerts.map { it.type }.toSet())
            val event = stock.alerts.first { it.type == Stock.ALERT_EVENT }
            assertTrue(event.message.contains("2026-") || event.message.contains("%"), stock.symbol)
            assertTrue(stock.chartPoints.size >= 20)
            assertEquals(stock.price, stock.chartPoints.last().price)
            assertTrue(stock.buyPoint > 0.0)
            assertTrue(stock.sellPoint > stock.buyPoint || stock.aiAdvice == Stock.ADVICE_SELL)
            assertTrue(stock.adviceReasons.size in 2..3, stock.symbol)
            stock.adviceReasons.forEach { reason ->
                assertTrue(reason.isNotBlank(), stock.symbol)
            }
            assertTrue(stock.trendConfidenceShort in 0..100, stock.symbol)
            assertTrue(stock.trendConfidenceMid in 0..100, stock.symbol)
            assertTrue(stock.trendConfidenceLong in 0..100, stock.symbol)
            assertTrue(stock.trendNarrative.isNotBlank(), stock.symbol)
            assertTrue(stock.trendNarrative.contains(stock.shortTrend), stock.symbol)
            assertTrue(stock.briefingBullets.size in 1..2, stock.symbol)
            stock.briefingBullets.forEach { bullet ->
                assertTrue(bullet.isNotBlank(), stock.symbol)
            }
            assertTrue(stock.briefingBullets.first().contains(stock.name), stock.symbol)
            assertTrue(
                stock.briefingBullets.first().contains(QuoteFormat.price(stock.price)),
                stock.symbol
            )
            assertTrue(stock.sentimentScore in 0..100, stock.symbol)
            assertTrue(stock.newsPositive >= 0, stock.symbol)
            assertTrue(stock.newsNeutral >= 0, stock.symbol)
            assertTrue(stock.newsNegative >= 0, stock.symbol)
            val newsTotal = stock.newsPositive + stock.newsNeutral + stock.newsNegative
            assertTrue(newsTotal >= 5, stock.symbol)
            when (stock.aiAdvice) {
                Stock.ADVICE_BUY -> {
                    assertTrue(stock.newsPositive > stock.newsNegative, stock.symbol)
                    assertTrue(stock.sentimentScore >= 55, stock.symbol)
                }
                Stock.ADVICE_SELL -> {
                    assertTrue(stock.newsNegative >= stock.newsPositive, stock.symbol)
                    assertTrue(stock.sentimentScore <= 50, stock.symbol)
                }
                else -> assertTrue(stock.newsNeutral >= stock.newsPositive || stock.newsNeutral >= stock.newsNegative, stock.symbol)
            }
            assertTrue(stock.upsideProbability5d in 0..100, stock.symbol)
            if (stock.aiAdvice == Stock.ADVICE_SELL) {
                assertTrue(stock.upsideProbability5d <= 70, stock.symbol)
            }
            if (stock.aiAdvice == Stock.ADVICE_BUY) {
                assertTrue(stock.upsideProbability5d >= 50, stock.symbol)
            }
            assertTrue(stock.reviewSummary.isNotBlank(), stock.symbol)
            assertTrue(stock.reviewSummary.contains("复盘"), stock.symbol)
            assertTrue(stock.reviewSummary.contains("开盘"), stock.symbol)
            assertTrue(stock.reviewSummary.contains("最高"), stock.symbol)
            assertTrue(stock.reviewSummary.contains("最低"), stock.symbol)
            assertTrue(stock.reviewSummary.contains("收盘"), stock.symbol)
            assertTrue(stock.reviewSummary.contains("涨跌幅"), stock.symbol)
            assertTrue(stock.reviewSummary.contains(stock.shortTrend), stock.symbol)
            assertNotEquals(stock.aiSummary, stock.reviewSummary, stock.symbol)
            assertTrue(stock.chainPeers.any { it.relation == ChainPeer.UPSTREAM }, stock.symbol)
            assertTrue(stock.chainPeers.any { it.relation == ChainPeer.DOWNSTREAM }, stock.symbol)
            stock.chainPeers.forEach { peer ->
                val listed = repo.getStock(peer.symbol)
                assertNotNull(listed, peer.symbol)
                assertEquals(listed!!.name, peer.name, peer.symbol)
                assertEquals(listed.changePercent, peer.changePercent, 0.0001, peer.symbol)
            }
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
        val hk = repo.getStock("00700")
        assertNotNull(hk)
        assertEquals(Stock.MARKET_HK, hk.market)
        val us = repo.getStock("AAPL")
        assertNotNull(us)
        assertEquals(Stock.MARKET_US, us.market)
    }

    @Test
    fun everyStockFillsPhase5Fields() {
        val all = repo.getStocksByMarket(Stock.MARKET_CN) +
            repo.getStocksByMarket(Stock.MARKET_HK) +
            repo.getStocksByMarket(Stock.MARKET_US)
        assertEquals(90, all.size)
        all.forEach { stock ->
            assertTrue(stock.market in setOf(Stock.MARKET_CN, Stock.MARKET_HK, Stock.MARKET_US), stock.symbol)
            assertTrue(stock.sector.isNotBlank(), stock.symbol)
            assertTrue(stock.tags.isNotEmpty(), stock.symbol)
            assertTrue(stock.popularity in 1..100, stock.symbol)
            assertTrue(stock.turnoverRate > 0.0, stock.symbol)
            assertTrue(stock.marketCap > 0.0, stock.symbol)
            assertTrue(stock.week52High >= stock.high, stock.symbol)
            assertTrue(stock.week52Low <= stock.low, stock.symbol)
            assertTrue(stock.revenue > 0.0, stock.symbol)
        }
        assertTrue(all.any { it.revenueGrowth > 0.0 })
        assertTrue(all.any { it.revenueGrowth < 0.0 })
    }

    @Test
    fun getStocksByMarketReturnsExpectedCounts() {
        val cn = repo.getStocksByMarket(Stock.MARKET_CN)
        val hk = repo.getStocksByMarket(Stock.MARKET_HK)
        val us = repo.getStocksByMarket(Stock.MARKET_US)
        assertEquals(50, cn.size)
        assertEquals(20, hk.size)
        assertEquals(20, us.size)
        assertEquals(cn, repo.getStocks())
        assertTrue(hk.all { it.market == Stock.MARKET_HK })
        assertTrue(us.all { it.market == Stock.MARKET_US })
        assertEquals(20, hk.map { it.symbol }.toSet().size)
        assertEquals(20, us.map { it.symbol }.toSet().size)
        assertEquals(emptyList(), repo.getStocksByMarket("未知市场"))
    }

    @Test
    fun getIndexesAndSectorsCoverMarkets() {
        val allIndexes = repo.getIndexes()
        assertEquals(5, allIndexes.size)
        assertEquals(5, allIndexes.map { it.symbol }.toSet().size)
        allIndexes.forEach { index ->
            assertTrue(index.name.isNotBlank())
            assertTrue(index.price > 0.0)
        }
        assertEquals(3, repo.getIndexes(Stock.MARKET_CN).size)
        assertEquals(1, repo.getIndexes(Stock.MARKET_HK).size)
        assertEquals(1, repo.getIndexes(Stock.MARKET_US).size)

        fun assertSectorCount(market: String) {
            val sectors = repo.getSectors(market)
            assertTrue(sectors.size in 5..8, "$market size=${sectors.size}")
            assertTrue(sectors.all { it.market == market })
            sectors.forEach { sector ->
                assertTrue(sector.name.isNotBlank())
                assertTrue(sector.leadStockName.isNotBlank())
                assertTrue(sector.leadStockSymbol.isNotBlank())
            }
        }
        assertSectorCount(Stock.MARKET_CN)
        assertSectorCount(Stock.MARKET_HK)
        assertSectorCount(Stock.MARKET_US)
        assertEquals(
            repo.getSectors(Stock.MARKET_CN).size +
                repo.getSectors(Stock.MARKET_HK).size +
                repo.getSectors(Stock.MARKET_US).size,
            repo.getSectors().size
        )
    }

    @Test
    fun getHotStocksSortsByPopularityDescending() {
        val hot = repo.getHotStocks(Stock.MARKET_CN)
        assertEquals(50, hot.size)
        assertEquals(hot.map { it.popularity }, hot.map { it.popularity }.sortedDescending())
        assertEquals(20, repo.getHotStocks(Stock.MARKET_HK).size)
        assertEquals(20, repo.getHotStocks(Stock.MARKET_US).size)
    }

    @Test
    fun getChartDataReturnsUsableCandles() {
        val symbol = repo.getStocks().first().symbol
        ChartPeriod.entries.forEach { period ->
            ChartType.entries.forEach { type ->
                AdjustType.entries.forEach { adjust ->
                    val candles = repo.getChartData(symbol, period, type, adjust)
                    assertTrue(candles.isNotEmpty(), "$period $type $adjust")
                    candles.forEach { candle ->
                        assertTrue(candle.time.isNotBlank())
                        assertTrue(candle.high >= maxOf(candle.open, candle.close), candle.time)
                        assertTrue(candle.low <= minOf(candle.open, candle.close), candle.time)
                        assertTrue(candle.volume > 0L)
                    }
                }
            }
        }
        assertTrue(repo.getChartData("NOPE", ChartPeriod.DAILY, ChartType.LINE, AdjustType.NONE).isEmpty())
    }

    @Test
    fun filterStocksEmptyCriteriaReturnsMarketList() {
        val cn = repo.getStocksByMarket(Stock.MARKET_CN)
        assertEquals(cn, repo.filterStocks(Stock.MARKET_CN, StockFilter.EMPTY))
        assertEquals(20, repo.filterStocks(Stock.MARKET_HK, StockFilter.EMPTY).size)
        assertEquals(emptyList(), repo.filterStocks("未知市场", StockFilter.EMPTY))
    }

    @Test
    fun filterStocksAppliesPriceChangeCapAndTags() {
        val sample = repo.getStocks().first { it.tags.contains("白酒") }
        val byPrice = repo.filterStocks(
            Stock.MARKET_CN,
            StockFilter(minPrice = sample.price, maxPrice = sample.price)
        )
        assertTrue(byPrice.isNotEmpty())
        assertTrue(byPrice.all { it.price == sample.price })

        val byChange = repo.filterStocks(
            Stock.MARKET_CN,
            StockFilter(minChangePercent = 0.0)
        )
        assertTrue(byChange.isNotEmpty())
        assertTrue(byChange.all { it.changePercent >= 0.0 })
        assertTrue(byChange.size < 50)

        val byCap = repo.filterStocks(
            Stock.MARKET_CN,
            StockFilter(minMarketCap = sample.marketCap, maxMarketCap = sample.marketCap)
        )
        assertTrue(byCap.all { it.marketCap == sample.marketCap })

        val byTag = repo.filterStocks(Stock.MARKET_CN, StockFilter(tags = setOf("白酒")))
        assertTrue(byTag.isNotEmpty())
        assertTrue(byTag.all { "白酒" in it.tags })
        assertTrue(byTag.any { it.symbol == sample.symbol })

        val none = repo.filterStocks(
            Stock.MARKET_CN,
            StockFilter(minPrice = 1_000_000.0)
        )
        assertTrue(none.isEmpty())
    }

    @Test
    fun reasoningStepsAndChartMarksVaryByStock() {
        val stocks = repo.getStocks()
        stocks.forEach { stock ->
            assertTrue(stock.aiReasoningSteps.size in 3..5, stock.symbol)
            assertTrue(stock.aiChartMarks.size in 3..5, stock.symbol)
            stock.aiReasoningSteps.forEach { step ->
                assertTrue(step.marker.isNotBlank(), stock.symbol)
                assertTrue(step.text.isNotBlank(), stock.symbol)
            }
            stock.aiChartMarks.forEach { mark ->
                assertTrue(mark.pointIndex in stock.chartPoints.indices, stock.symbol)
                assertTrue(mark.note.isNotBlank(), stock.symbol)
            }
        }
        val buy = stocks.first { it.aiAdvice == Stock.ADVICE_BUY }
        val sell = stocks.first { it.aiAdvice == Stock.ADVICE_SELL }
        val hold = stocks.first { it.aiAdvice == Stock.ADVICE_HOLD }
        assertNotEquals(buy.aiReasoningSteps.map { it.text }, sell.aiReasoningSteps.map { it.text })
        assertNotEquals(hold.aiReasoningSteps.map { it.text }, sell.aiReasoningSteps.map { it.text })
        assertTrue(stocks.map { it.aiReasoningSteps.size }.toSet().size >= 2)
    }

    @Test
    fun getStockPicksAndEtfsCoverFourCategories() {
        val company = repo.getStockPicks(StockPickCategory.COMPANY)
        val institution = repo.getStockPicks(StockPickCategory.INSTITUTION)
        val capital = repo.getStockPicks(StockPickCategory.CAPITAL)
        assertEquals(50, company.size)
        assertTrue(institution.size >= 5)
        assertTrue(capital.size >= 5)
        assertEquals(emptyList(), repo.getStockPicks(StockPickCategory.ETF))
        assertEquals(emptyList(), repo.getStockPicks("未知分类"))
        assertTrue(institution.all { it.symbol in company.map { stock -> stock.symbol } })

        val etfs = repo.getETFs()
        assertEquals(10, etfs.size)
        assertEquals(10, etfs.map { it.symbol }.toSet().size)
        etfs.forEach { etf ->
            assertTrue(etf.name.isNotBlank())
            assertTrue(etf.nav > 0.0)
            assertTrue(etf.iopv > 0.0)
            assertTrue(
                etf.category in setOf(
                    ETF.CATEGORY_BROAD,
                    ETF.CATEGORY_SECTOR,
                    ETF.CATEGORY_BOND
                )
            )
        }
        assertTrue(etfs.any { it.category == ETF.CATEGORY_BROAD })
        assertTrue(etfs.any { it.category == ETF.CATEGORY_SECTOR })
        assertTrue(etfs.any { it.category == ETF.CATEGORY_BOND })
    }

    @Test
    fun getRankingsAndFundHoldingsCoverFourBoards() {
        RankingType.ALL.forEach { type ->
            val rows = repo.getRankings(type)
            assertTrue(rows.size >= 10, type)
            assertEquals(rows.size, rows.map { it.symbol }.toSet().size, type)
            rows.forEachIndexed { index, row ->
                assertEquals(index + 1, row.rank, type)
                assertTrue(row.name.isNotBlank(), row.symbol)
                assertTrue(row.holdingRatio > 0.0, row.symbol)
                assertNotNull(repo.getStock(row.symbol))
            }
        }
        val heavy = repo.getRankings(RankingType.FUND_HEAVY).map { it.symbol }
        val increase = repo.getRankings(RankingType.PUBLIC_INCREASE).map { it.symbol }
        val fresh = repo.getRankings(RankingType.PUBLIC_NEW).map { it.symbol }
        assertNotEquals(heavy, increase)
        assertNotEquals(increase, fresh)
        assertTrue(repo.getRankings(RankingType.PUBLIC_INCREASE).all { it.holdingChange > 0.0 })
        assertTrue(repo.getRankings(RankingType.PUBLIC_NEW).all { it.holdingChange > 0.0 })
        assertEquals(emptyList(), repo.getRankings("未知榜单"))

        val symbol = repo.getStocks().first().symbol
        val holdings = repo.getFundHoldings(symbol)
        assertEquals(5, holdings.size)
        holdings.forEach { holding ->
            assertEquals(symbol, holding.symbol)
            assertTrue(holding.fundName.isNotBlank())
            assertTrue(holding.holdingRatio > 0.0)
            assertTrue(holding.marketValue > 0.0)
        }
        assertEquals(emptyList(), repo.getFundHoldings("999999"))
    }
}
