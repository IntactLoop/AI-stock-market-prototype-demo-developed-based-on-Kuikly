package com.example.myandroidapplication.data.repository

import com.example.myandroidapplication.data.model.AdjustType
import com.example.myandroidapplication.data.model.CandlePoint
import com.example.myandroidapplication.data.model.ChartMark
import com.example.myandroidapplication.data.model.ChartPeriod
import com.example.myandroidapplication.data.model.ChartPoint
import com.example.myandroidapplication.data.model.ChartType
import com.example.myandroidapplication.data.model.ETF
import com.example.myandroidapplication.data.model.FundHolding
import com.example.myandroidapplication.data.model.MarketIndex
import com.example.myandroidapplication.data.model.Ranking
import com.example.myandroidapplication.data.model.RankingType
import com.example.myandroidapplication.data.model.ReasoningStep
import com.example.myandroidapplication.data.model.Sector
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.model.StockFilter
import com.example.myandroidapplication.data.model.StockPickCategory
import com.example.myandroidapplication.data.model.WatchAlert
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sin

/**
 * 预生成沪深 50 + 港股 20 + 美股 20。每条都填满 AI 字段与 Phase 5 扩展字段。
 *
 * [getStocks] 仍只返回沪深 A 股，与 Phase 2 首页心智一致。
 *
 * 三种情景均覆盖：
 * - 趋势向上 + 接近支撑 → 买入
 * - 震荡 + 接近压力 → 观望
 * - 趋势向下 + 跌破支撑 → 卖出
 */
class MockStockRepository : StockRepository {

    private val aShares: List<Stock> by lazy { CN_SEEDS.mapIndexed { index, seed -> buildStock(seed, index) } }
    private val hkShares: List<Stock> by lazy { HK_SEEDS.mapIndexed { index, seed -> buildStock(seed, index) } }
    private val usShares: List<Stock> by lazy { US_SEEDS.mapIndexed { index, seed -> buildStock(seed, index) } }
    private val allStocks: List<Stock> by lazy { aShares + hkShares + usShares }

    override fun getStocks(): List<Stock> = aShares

    override fun getStock(symbol: String): Stock? = allStocks.find { it.symbol == symbol }

    override fun getIndexes(market: String?): List<MarketIndex> {
        return if (market == null) INDEXES else INDEXES.filter { it.market == market }
    }

    override fun getStocksByMarket(market: String): List<Stock> = when (market) {
        Stock.MARKET_CN -> aShares
        Stock.MARKET_HK -> hkShares
        Stock.MARKET_US -> usShares
        else -> emptyList()
    }

    override fun getSectors(market: String?): List<Sector> {
        return if (market == null) SECTORS else SECTORS.filter { it.market == market }
    }

    override fun getHotStocks(market: String): List<Stock> {
        return getStocksByMarket(market).sortedByDescending { it.popularity }
    }

    override fun getChartData(
        symbol: String,
        period: ChartPeriod,
        type: ChartType,
        adjust: AdjustType
    ): List<CandlePoint> {
        val stock = getStock(symbol) ?: return emptyList()
        val raw = candlesFor(stock, period, type)
        return adjustCandles(raw, adjust)
    }

    override fun filterStocks(market: String, criteria: StockFilter): List<Stock> {
        val source = getStocksByMarket(market)
        if (criteria.isEmpty()) return source
        return source.filter { criteria.matches(it) }
    }

    override fun getStockPicks(category: String): List<Stock> = when (category) {
        StockPickCategory.COMPANY -> aShares
        StockPickCategory.INSTITUTION -> aShares.filter { stock ->
            stock.tags.contains("高股息") ||
                stock.marketCap >= 4000.0 ||
                stock.sector in setOf("银行", "白酒", "金融")
        }
        StockPickCategory.CAPITAL -> aShares.filter { stock ->
            stock.tags.contains("热门") ||
                stock.volumeRatio >= 1.20 ||
                stock.turnoverRate >= 3.0
        }
        else -> emptyList()
    }

    override fun getETFs(): List<ETF> = ETFS

    override fun getRankings(type: String): List<Ranking> = when (type) {
        RankingType.FUND_HEAVY -> rankBy(aShares.sortedByDescending { it.marketCap }.take(15)) { index, _ ->
            Pair((8.80 - index * 0.38).coerceAtLeast(1.20), 1.65 - index * 0.22)
        }
        RankingType.PUBLIC_HEAVY -> rankBy(aShares.sortedByDescending { it.popularity }.take(15)) { index, _ ->
            Pair((7.40 - index * 0.32).coerceAtLeast(1.00), 0.95 - index * 0.16)
        }
        RankingType.PUBLIC_INCREASE -> rankBy(
            aShares.filter { it.changePercent > 0.0 }.sortedByDescending { it.volumeRatio }.take(12)
        ) { index, _ ->
            Pair((3.20 - index * 0.12).coerceAtLeast(0.60), 2.15 - index * 0.11)
        }
        RankingType.PUBLIC_NEW -> rankBy(
            aShares.sortedByDescending { it.turnoverRate }.take(12)
        ) { index, _ ->
            Pair((1.80 - index * 0.08).coerceAtLeast(0.35), 0.85 - index * 0.04)
        }
        else -> emptyList()
    }

    override fun getFundHoldings(symbol: String): List<FundHolding> {
        val stock = getStock(symbol) ?: return emptyList()
        val start = symbol.hashCode().and(0x7fffffff) % FUND_NAMES.size
        return (0 until 5).map { offset ->
            FundHolding(
                fundName = FUND_NAMES[(start + offset) % FUND_NAMES.size],
                symbol = stock.symbol,
                holdingRatio = (4.80 - offset * 0.65).coerceAtLeast(0.40).round2(),
                holdingChange = (1.35 - offset * 0.48).round2(),
                marketValue = (96.0 - offset * 14.0).coerceAtLeast(8.0).round2()
            )
        }
    }

    private fun rankBy(
        stocks: List<Stock>,
        metrics: (index: Int, stock: Stock) -> Pair<Double, Double>
    ): List<Ranking> = stocks.mapIndexed { index, stock ->
        val (ratio, change) = metrics(index, stock)
        Ranking(
            rank = index + 1,
            symbol = stock.symbol,
            name = stock.name,
            holdingRatio = ratio.round2(),
            holdingChange = change.round2()
        )
    }

    private data class Seed(
        val symbol: String,
        val name: String,
        val basePrice: Double,
        val pe: Double,
        val pb: Double,
        val market: String = Stock.MARKET_CN,
        val sector: String = "",
        val marketCap: Double = 0.0
    )

    private enum class Scenario { BULL_SUPPORT, RANGE_RESISTANCE, BEAR_BREAK }

    private fun scenarioOf(index: Int): Scenario = when {
        index < 17 -> Scenario.BULL_SUPPORT
        index < 34 -> Scenario.RANGE_RESISTANCE
        else -> Scenario.BEAR_BREAK
    }

    private fun buildStock(seed: Seed, index: Int): Stock {
        val scenario = scenarioOf(index)
        val base = seed.basePrice
        val volume = 8_000_000L + index * 137_000L
        val volumeRatio = when (scenario) {
            Scenario.BULL_SUPPORT -> (1.18 + (index % 6) * 0.08).round2()
            Scenario.RANGE_RESISTANCE -> (0.92 + (index % 5) * 0.06).round2()
            Scenario.BEAR_BREAK -> (1.25 + (index % 4) * 0.10).round2()
        }
        val volumePct = abs(((volumeRatio - 1.0) * 100.0).toInt())

        val changePercent = when (scenario) {
            Scenario.BULL_SUPPORT -> (0.62 + (index % 9) * 0.18).round2()
            Scenario.RANGE_RESISTANCE -> (-0.35 + (index % 8) * 0.10).round2()
            Scenario.BEAR_BREAK -> (-0.80 - (index % 7) * 0.15).round2()
        }
        val price = base.round2()
        val change = (price * changePercent / 100.0).round2()
        val close = price
        val open = (price - change * 0.55).round2()
        val high = (maxOf(open, close) + abs(change) * 0.45).round2()
        val low = (minOf(open, close) - abs(change) * 0.50).round2()

        val (support, resistance, ma5, ma10, ma20, macd, rsi) = when (scenario) {
            Scenario.BULL_SUPPORT -> Indicators(
                support = (price * 0.987).round2(),
                resistance = (price * 1.042).round2(),
                ma5 = (price * 0.997).round2(),
                ma10 = (price * 0.986).round2(),
                ma20 = (price * 0.968).round2(),
                macd = (0.18 + (index % 5) * 0.04).round2(),
                rsi = (52.0 + (index % 11).toDouble()).round2()
            )
            Scenario.RANGE_RESISTANCE -> Indicators(
                support = (price * 0.955).round2(),
                resistance = (price * 1.012).round2(),
                ma5 = (price * 1.002).round2(),
                ma10 = (price * 0.999).round2(),
                ma20 = (price * 1.004).round2(),
                macd = (-0.04 + (index % 5) * 0.02).round2(),
                rsi = (66.0 + (index % 8).toDouble()).round2()
            )
            Scenario.BEAR_BREAK -> Indicators(
                support = (price * 1.018).round2(),
                resistance = (price * 1.055).round2(),
                ma5 = (price * 1.012).round2(),
                ma10 = (price * 1.028).round2(),
                ma20 = (price * 1.048).round2(),
                macd = (-0.22 - (index % 5) * 0.03).round2(),
                rsi = (28.0 + (index % 10).toDouble()).round2()
            )
        }

        val signals = when (scenario) {
            Scenario.BULL_SUPPORT -> buildList {
                add("MACD金叉")
                add("均线多头")
                if (volumeRatio >= 1.30) add("放量")
            }
            Scenario.RANGE_RESISTANCE -> buildList {
                add("接近压力位")
                if (rsi >= 70.0) add("RSI超买") else add("高位震荡")
            }
            Scenario.BEAR_BREAK -> listOf("MACD死叉", "跌破支撑", "均线空头")
        }

        val (advice, risk, shortTrend, midTrend, interpretation, reason) = when (scenario) {
            Scenario.BULL_SUPPORT -> AdvicePack(
                advice = Stock.ADVICE_BUY,
                risk = if (rsi >= 62.0) Stock.RISK_MID else Stock.RISK_LOW,
                shortTrend = "短期偏多",
                midTrend = "中期偏多",
                interpretation = "短期动能转强，但需关注成交量是否持续配合",
                reason = "均线多头且价格贴近支撑 ${support.px()}，适合在支撑附近分批关注，止盈看压力 ${resistance.px()}"
            )
            Scenario.RANGE_RESISTANCE -> AdvicePack(
                advice = Stock.ADVICE_HOLD,
                risk = Stock.RISK_MID,
                shortTrend = "短期震荡",
                midTrend = "中期震荡",
                interpretation = "接近压力位，突破需放量；RSI 偏高，注意回调",
                reason = "价格靠近压力 ${resistance.px()}，方向未明，等待放量突破或回落至支撑 ${support.px()} 再决策"
            )
            Scenario.BEAR_BREAK -> AdvicePack(
                advice = Stock.ADVICE_SELL,
                risk = Stock.RISK_HIGH,
                shortTrend = "短期偏空",
                midTrend = "中期偏空",
                interpretation = "均线空头排列且跌破支撑，短期风险偏大",
                reason = "已跌破支撑 ${support.px()}，MACD 位于零轴下方，优先控制仓位，反弹至 ${resistance.px()} 附近再评估"
            )
        }

        val fundVerb = if (volumeRatio >= 1.0) "放大" else "萎缩"
        val alerts = listOf(
            when (scenario) {
                Scenario.BULL_SUPPORT -> WatchAlert(
                    Stock.ALERT_RISK,
                    "当前股价接近支撑位 ${support.px()}，关注企稳信号"
                )
                Scenario.RANGE_RESISTANCE -> WatchAlert(
                    Stock.ALERT_RISK,
                    "当前股价接近压力位 ${resistance.px()}，注意回调风险"
                )
                Scenario.BEAR_BREAK -> WatchAlert(
                    Stock.ALERT_RISK,
                    "股价跌破支撑位 ${support.px()}，注意下行风险"
                )
            },
            WatchAlert(
                Stock.ALERT_FUND,
                "成交量较昨日同期${fundVerb} ${volumePct}%，资金关注度${if (volumeRatio >= 1.0) "提升" else "回落"}"
            ),
            WatchAlert(
                Stock.ALERT_TREND,
                when (scenario) {
                    Scenario.BULL_SUPPORT -> "短期均线多头排列，趋势偏强"
                    Scenario.RANGE_RESISTANCE -> "均线缠绕，短期方向未明"
                    Scenario.BEAR_BREAK -> "短期均线空头排列，趋势偏弱"
                }
            )
        )

        val confidence = when (scenario) {
            Scenario.BULL_SUPPORT -> 68 + index % 18
            Scenario.RANGE_RESISTANCE -> 55 + index % 16
            Scenario.BEAR_BREAK -> 70 + index % 15
        }.coerceIn(0, 100)

        val changePctText = when {
            changePercent > 0.0 -> "+${changePercent.px()}%"
            else -> "${changePercent.px()}%"
        }
        val summary =
            "今日该股开盘 ${open.px()}，最高 ${high.px()}，最低 ${low.px()}，收盘 ${close.px()}，" +
                "涨跌幅 $changePctText，成交量较昨日${fundVerb} ${volumePct}%，$shortTrend。"

        val sector = seed.sector.ifBlank { defaultSector(seed.market, index) }
        val popularity = (100 - index).coerceIn(1, 100)
        val turnoverRate = when (scenario) {
            Scenario.BULL_SUPPORT -> (1.80 + (index % 7) * 0.35).round2()
            Scenario.RANGE_RESISTANCE -> (1.10 + (index % 6) * 0.22).round2()
            Scenario.BEAR_BREAK -> (2.40 + (index % 5) * 0.40).round2()
        }
        val sharesYi = 10.0 + index * 1.8
        val marketCap = if (seed.marketCap > 0.0) seed.marketCap.round2() else (price * sharesYi).round2()
        val week52High = maxOf(high, (price * 1.16).round2())
        val week52Low = minOf(low, (price * 0.82).round2())
        val revenue = (marketCap * (0.16 + (index % 9) * 0.035)).round2()
        val revenueGrowth = when (scenario) {
            Scenario.BULL_SUPPORT -> (6.5 + (index % 11) * 1.15).round2()
            Scenario.RANGE_RESISTANCE -> (-1.2 + (index % 8) * 0.75).round2()
            Scenario.BEAR_BREAK -> (-5.8 - (index % 7) * 0.85).round2()
        }
        val tags = buildTags(sector, popularity, scenario, seed.pe)
        val chartPoints = buildChartPoints(price, volume, scenario, index)
        val reasoningSteps = buildReasoningSteps(
            scenario = scenario,
            price = price,
            support = support,
            resistance = resistance,
            ma5 = ma5,
            ma10 = ma10,
            macd = macd,
            rsi = rsi,
            volumeRatio = volumeRatio,
            seed = index
        )
        val chartMarks = buildChartMarks(
            points = chartPoints,
            scenario = scenario,
            support = support,
            resistance = resistance,
            seed = index
        )
        val adviceReasons = buildAdviceReasons(
            scenario = scenario,
            price = price,
            support = support,
            resistance = resistance,
            ma5 = ma5,
            ma10 = ma10,
            macd = macd,
            rsi = rsi,
            volumeRatio = volumeRatio
        )
        val trendConfidenceShort = when (scenario) {
            Scenario.BULL_SUPPORT -> (74 + index % 12).coerceIn(0, 100)
            Scenario.RANGE_RESISTANCE -> (52 + index % 10).coerceIn(0, 100)
            Scenario.BEAR_BREAK -> (76 + index % 11).coerceIn(0, 100)
        }
        val trendConfidenceMid = when (scenario) {
            Scenario.BULL_SUPPORT -> (66 + index % 10).coerceIn(0, 100)
            Scenario.RANGE_RESISTANCE -> (48 + index % 9).coerceIn(0, 100)
            Scenario.BEAR_BREAK -> (70 + index % 9).coerceIn(0, 100)
        }
        val trendConfidenceLong = when (scenario) {
            Scenario.BULL_SUPPORT -> (58 + index % 9).coerceIn(0, 100)
            Scenario.RANGE_RESISTANCE -> (44 + index % 8).coerceIn(0, 100)
            Scenario.BEAR_BREAK -> (62 + index % 8).coerceIn(0, 100)
        }
        val trendNarrative = when (scenario) {
            Scenario.BULL_SUPPORT ->
                "MA5 ${ma5.px()} 高于 MA10 ${ma10.px()}、MA20 ${ma20.px()}，涨跌幅 $changePctText，" +
                    "$shortTrend、$midTrend，趋势延续概率偏高。"
            Scenario.RANGE_RESISTANCE ->
                "均线缠绕（MA5 ${ma5.px()} / MA10 ${ma10.px()}），现价靠近压力 ${resistance.px()}，" +
                    "$shortTrend、$midTrend，等待方向选择。"
            Scenario.BEAR_BREAK ->
                "MA5 ${ma5.px()} 低于 MA10 ${ma10.px()}，已跌破支撑 ${support.px()}，涨跌幅 $changePctText，" +
                    "$shortTrend、$midTrend，下行风险仍在。"
        }

        return Stock(
            symbol = seed.symbol,
            name = seed.name,
            price = price,
            change = change,
            changePercent = changePercent,
            open = open.round2(),
            high = high,
            low = low,
            close = close,
            volume = volume,
            turnover = (price * volume).round2(),
            pe = seed.pe.round2(),
            pb = seed.pb.round2(),
            ma5 = ma5,
            ma10 = ma10,
            ma20 = ma20,
            macd = macd,
            rsi = rsi,
            support = support,
            resistance = resistance,
            aiSignal = signals.first(),
            aiConfidence = confidence,
            aiAdvice = advice,
            riskLevel = risk,
            buyPoint = support,
            sellPoint = resistance,
            aiSummary = summary,
            volumeRatio = volumeRatio,
            signals = signals,
            aiInterpretation = interpretation,
            adviceReason = reason,
            shortTrend = shortTrend,
            midTrend = midTrend,
            alerts = alerts,
            chartPoints = chartPoints,
            market = seed.market,
            sector = sector,
            tags = tags,
            popularity = popularity,
            turnoverRate = turnoverRate,
            marketCap = marketCap,
            week52High = week52High,
            week52Low = week52Low,
            aiReasoningSteps = reasoningSteps,
            aiChartMarks = chartMarks,
            revenue = revenue,
            revenueGrowth = revenueGrowth,
            adviceReasons = adviceReasons,
            trendConfidenceShort = trendConfidenceShort,
            trendConfidenceMid = trendConfidenceMid,
            trendConfidenceLong = trendConfidenceLong,
            trendNarrative = trendNarrative
        )
    }

    private fun buildChartPoints(
        lastPrice: Double,
        lastVolume: Long,
        scenario: Scenario,
        seed: Int
    ): List<ChartPoint> {
        val last = CHART_DAYS.lastIndex
        return CHART_DAYS.mapIndexed { i, day ->
            val t = i / last.toDouble()
            val wave = ((i * 17 + seed * 3) % 7 - 3) * lastPrice * 0.0018
            val trend = when (scenario) {
                Scenario.BULL_SUPPORT -> lastPrice * (0.955 + 0.045 * t)
                Scenario.RANGE_RESISTANCE -> lastPrice * (1.0 + 0.012 * sin(i * 0.9))
                Scenario.BEAR_BREAK -> lastPrice * (1.055 - 0.055 * t)
            }
            val price = if (i == last) lastPrice else (trend + wave).round2()
            val volume = (lastVolume * (0.72 + 0.05 * ((i * 13 + seed) % 8))).toLong()
            ChartPoint(time = day, price = price, volume = volume)
        }
    }

    private fun buildAdviceReasons(
        scenario: Scenario,
        price: Double,
        support: Double,
        resistance: Double,
        ma5: Double,
        ma10: Double,
        macd: Double,
        rsi: Double,
        volumeRatio: Double
    ): List<String> = when (scenario) {
        Scenario.BULL_SUPPORT -> buildList {
            add("现价 ${price.px()} 贴近支撑 ${support.px()}，回调空间有限")
            add("MA5 ${ma5.px()} 位于 MA10 ${ma10.px()} 上方，短期均线多头")
            add(
                if (volumeRatio >= 1.30) {
                    "量比 ${volumeRatio.px()}，资金配合较好，买入点看 ${support.px()}"
                } else {
                    "建议支撑买入价 ${support.px()}，压力看 ${resistance.px()}"
                }
            )
        }
        Scenario.RANGE_RESISTANCE -> listOf(
            "现价 ${price.px()} 靠近压力 ${resistance.px()}，方向未明",
            "RSI ${rsi.px()}，注意高位震荡与回落",
            "等待回落至支撑 ${support.px()} 或放量突破后再决策"
        )
        Scenario.BEAR_BREAK -> listOf(
            "已跌破支撑 ${support.px()}，现价 ${price.px()}，风险偏高",
            "MACD ${macd.px()} 位于零轴下方，均线空头",
            "反弹至压力 ${resistance.px()} 附近再评估仓位"
        )
    }

    private fun defaultSector(market: String, index: Int): String = when (market) {
        Stock.MARKET_HK -> HK_SECTOR_NAMES[index % HK_SECTOR_NAMES.size]
        Stock.MARKET_US -> US_SECTOR_NAMES[index % US_SECTOR_NAMES.size]
        else -> CN_SECTOR_NAMES[index % CN_SECTOR_NAMES.size]
    }

    private fun buildTags(
        sector: String,
        popularity: Int,
        scenario: Scenario,
        pe: Double
    ): List<String> = buildList {
        add(sector)
        if (popularity >= 85) add("热门")
        when (scenario) {
            Scenario.BULL_SUPPORT -> add("成长")
            Scenario.RANGE_RESISTANCE -> add("震荡")
            Scenario.BEAR_BREAK -> add("调整")
        }
        if (pe in 0.1..12.0) add("价值")
        if (sector == "白酒" || sector == "银行" || sector == "金融") add("高股息")
    }.distinct()

    private fun buildReasoningSteps(
        scenario: Scenario,
        price: Double,
        support: Double,
        resistance: Double,
        ma5: Double,
        ma10: Double,
        macd: Double,
        rsi: Double,
        volumeRatio: Double,
        seed: Int
    ): List<ReasoningStep> {
        val target = 3 + seed % 3
        val core = when (scenario) {
            Scenario.BULL_SUPPORT -> buildList {
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_NEXT,
                        "MACD 金叉（${macd.px()}），短期动能转强"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_OK,
                        "MA5 ${ma5.px()} 位于 MA10 ${ma10.px()} 上方"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_OK,
                        "价格 ${price.px()} 贴近支撑 ${support.px()}"
                    )
                )
                if (volumeRatio >= 1.30) {
                    add(
                        ReasoningStep(
                            ReasoningStep.MARK_OK,
                            "量比 ${volumeRatio.px()}，放量配合上涨"
                        )
                    )
                } else {
                    add(
                        ReasoningStep(
                            ReasoningStep.MARK_WARN,
                            "量比 ${volumeRatio.px()}，需确认成交是否持续"
                        )
                    )
                }
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_NEXT,
                        "上行空间看压力 ${resistance.px()}"
                    )
                )
            }
            Scenario.RANGE_RESISTANCE -> buildList {
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_NEXT,
                        "价格 ${price.px()} 接近压力 ${resistance.px()}"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_WARN,
                        "RSI ${rsi.px()} 偏高，注意回调"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_NEXT,
                        "等待放量突破或回落支撑 ${support.px()}"
                    )
                )
                if (rsi >= 70.0) {
                    add(
                        ReasoningStep(
                            ReasoningStep.MARK_WARN,
                            "RSI 进入超买区，不宜追高"
                        )
                    )
                } else {
                    add(
                        ReasoningStep(
                            ReasoningStep.MARK_NEXT,
                            "高位震荡，方向未明，维持观望"
                        )
                    )
                }
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_OK,
                        "压力 ${resistance.px()} 与支撑 ${support.px()} 构成箱体"
                    )
                )
            }
            Scenario.BEAR_BREAK -> buildList {
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_WARN,
                        "价格 ${price.px()} 跌破支撑 ${support.px()}"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_OK,
                        "MACD 死叉（${macd.px()}），空头占优"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_NEXT,
                        "MA5 ${ma5.px()} 低于 MA10 ${ma10.px()}，均线空头"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_WARN,
                        "风险偏高，优先控制仓位"
                    )
                )
                add(
                    ReasoningStep(
                        ReasoningStep.MARK_NEXT,
                        "反弹至 ${resistance.px()} 附近再评估"
                    )
                )
            }
        }
        return core.take(target.coerceIn(3, 5))
    }

    private fun buildChartMarks(
        points: List<ChartPoint>,
        scenario: Scenario,
        support: Double,
        resistance: Double,
        seed: Int
    ): List<ChartMark> {
        if (points.size < 8) return emptyList()
        val last = points.lastIndex
        val volPeak = points.indices.maxBy { i ->
            if (i == last) Long.MIN_VALUE else points[i].volume
        }
        val nearSupport = points.indices.minBy { abs(points[it].price - support) }
        val nearResist = points.indices.minBy { abs(points[it].price - resistance) }
        val mid = last / 2
        val late = (last * 3 / 4).coerceAtMost(last - 1)
        val candidates = when (scenario) {
            Scenario.BULL_SUPPORT -> listOf(
                ChartMark(
                    pointIndex = nearSupport.coerceIn(1, last - 1),
                    type = ChartMark.TYPE_BUY,
                    label = "买入",
                    note = "贴近支撑 ${support.px()}，适合分批关注"
                ),
                ChartMark(
                    pointIndex = volPeak,
                    type = ChartMark.TYPE_VOLUME,
                    label = "放量",
                    note = "该日成交放大，资金关注度提升"
                ),
                ChartMark(
                    pointIndex = last,
                    type = ChartMark.TYPE_RESISTANCE,
                    label = "压力",
                    note = "上方压力 ${resistance.px()}，突破需放量"
                ),
                ChartMark(
                    pointIndex = mid,
                    type = ChartMark.TYPE_BUY,
                    label = "动能",
                    note = "中段抬升，均线多头延续"
                ),
                ChartMark(
                    pointIndex = late,
                    type = ChartMark.TYPE_VOLUME,
                    label = "回踩",
                    note = "回踩后量能仍在，趋势未破坏"
                )
            )
            Scenario.RANGE_RESISTANCE -> listOf(
                ChartMark(
                    pointIndex = nearResist.coerceIn(1, last),
                    type = ChartMark.TYPE_RESISTANCE,
                    label = "压力",
                    note = "价格靠近压力 ${resistance.px()}，注意回调"
                ),
                ChartMark(
                    pointIndex = volPeak,
                    type = ChartMark.TYPE_VOLUME,
                    label = "放量",
                    note = "冲高放量，方向仍待确认"
                ),
                ChartMark(
                    pointIndex = nearSupport.coerceIn(0, last - 2),
                    type = ChartMark.TYPE_SUPPORT,
                    label = "支撑",
                    note = "箱体下沿支撑 ${support.px()}"
                ),
                ChartMark(
                    pointIndex = mid,
                    type = ChartMark.TYPE_VOLUME,
                    label = "震荡",
                    note = "中段量能平稳，高位震荡"
                ),
                ChartMark(
                    pointIndex = last,
                    type = ChartMark.TYPE_RESISTANCE,
                    label = "现价",
                    note = "现价接近压力，突破需放量"
                )
            )
            Scenario.BEAR_BREAK -> listOf(
                ChartMark(
                    pointIndex = late,
                    type = ChartMark.TYPE_BREAK,
                    label = "跌破",
                    note = "跌破支撑 ${support.px()}，短期风险偏大"
                ),
                ChartMark(
                    pointIndex = volPeak,
                    type = ChartMark.TYPE_VOLUME,
                    label = "放量",
                    note = "下跌放量，空头动能占优"
                ),
                ChartMark(
                    pointIndex = last,
                    type = ChartMark.TYPE_BREAK,
                    label = "现价",
                    note = "价格仍在支撑下方，优先控制仓位"
                ),
                ChartMark(
                    pointIndex = mid,
                    type = ChartMark.TYPE_RESISTANCE,
                    label = "反弹",
                    note = "中段反弹高点接近压力 ${resistance.px()}"
                ),
                ChartMark(
                    pointIndex = (last / 4).coerceAtLeast(1),
                    type = ChartMark.TYPE_VOLUME,
                    label = "转弱",
                    note = "量能转弱后趋势转空"
                )
            )
        }
        val target = 3 + seed % 3
        val unique = mutableListOf<ChartMark>()
        val used = mutableSetOf<Int>()
        candidates.forEach { mark ->
            val index = mark.pointIndex.coerceIn(0, last)
            if (index !in used) {
                used += index
                unique += mark.copy(pointIndex = index)
            }
        }
        val targetCount = target.coerceIn(3, 5)
        var fill = 0
        while (unique.size < targetCount && fill <= last) {
            if (fill !in used) {
                used += fill
                unique += ChartMark(
                    pointIndex = fill,
                    type = ChartMark.TYPE_VOLUME,
                    label = "观察",
                    note = "${points[fill].time} 价格 ${points[fill].price.px()}"
                )
            }
            fill++
        }
        return unique.take(targetCount)
    }

    private fun candlesFor(
        stock: Stock,
        period: ChartPeriod,
        type: ChartType
    ): List<CandlePoint> {
        val points = stock.chartPoints
        if (points.isEmpty()) return emptyList()
        val daily = points.toCandles()
        val selected = when (period) {
            ChartPeriod.INTRADAY -> buildIntraday(stock)
            ChartPeriod.FIVE_DAY -> daily.takeLast(5)
            ChartPeriod.DAILY -> daily
            ChartPeriod.WEEKLY -> aggregate(daily, 4)
            ChartPeriod.MONTHLY -> aggregate(daily, 8)
        }
        return if (type == ChartType.LINE || selected.isNotEmpty()) selected else daily
    }

    private fun List<ChartPoint>.toCandles(): List<CandlePoint> {
        var prev = first().price
        return map { point ->
            val close = point.price
            val open = prev
            val high = maxOf(open, close) * 1.006
            val low = minOf(open, close) * 0.994
            prev = close
            CandlePoint(
                time = point.time,
                open = open.round2(),
                high = high.round2(),
                low = low.round2(),
                close = close,
                volume = point.volume
            )
        }
    }

    private fun buildIntraday(stock: Stock): List<CandlePoint> {
        val last = stock.price
        val baseVol = (stock.volume / INTRADAY_LABELS.size).coerceAtLeast(1L)
        var prev = (last * 0.992).round2()
        return INTRADAY_LABELS.mapIndexed { i, label ->
            val t = i / (INTRADAY_LABELS.lastIndex.coerceAtLeast(1)).toDouble()
            val close = (last * (0.992 + 0.008 * t) + sin(i * 0.7) * last * 0.0015).round2()
            val open = prev
            val high = maxOf(open, close) * 1.003
            val low = minOf(open, close) * 0.997
            prev = close
            CandlePoint(
                time = label,
                open = open.round2(),
                high = high.round2(),
                low = low.round2(),
                close = close,
                volume = (baseVol * (0.8 + 0.05 * (i % 6))).toLong()
            )
        }
    }

    private fun aggregate(daily: List<CandlePoint>, bucket: Int): List<CandlePoint> {
        if (daily.isEmpty()) return emptyList()
        return daily.chunked(bucket).map { group ->
            CandlePoint(
                time = group.last().time,
                open = group.first().open,
                high = group.maxOf { it.high },
                low = group.minOf { it.low },
                close = group.last().close,
                volume = group.sumOf { it.volume }
            )
        }
    }

    private fun adjustCandles(candles: List<CandlePoint>, adjust: AdjustType): List<CandlePoint> {
        if (candles.isEmpty() || adjust == AdjustType.NONE) return candles
        val last = candles.lastIndex
        val factor = when (adjust) {
            AdjustType.FORWARD -> 0.97
            AdjustType.BACKWARD -> 1.04
            AdjustType.NONE -> 1.0
        }
        return candles.mapIndexed { i, candle ->
            if (i == last) candle else candle.scale(factor)
        }
    }

    private fun CandlePoint.scale(factor: Double): CandlePoint = copy(
        open = (open * factor).round2(),
        high = (high * factor).round2(),
        low = (low * factor).round2(),
        close = (close * factor).round2()
    )

    private data class Indicators(
        val support: Double,
        val resistance: Double,
        val ma5: Double,
        val ma10: Double,
        val ma20: Double,
        val macd: Double,
        val rsi: Double
    )

    private data class AdvicePack(
        val advice: String,
        val risk: String,
        val shortTrend: String,
        val midTrend: String,
        val interpretation: String,
        val reason: String
    )

    companion object {
        private val CHART_DAYS = listOf(
            "08-18", "08-19", "08-20", "08-21", "08-22",
            "08-25", "08-26", "08-27", "08-28", "08-29",
            "09-01", "09-02", "09-03", "09-04", "09-05",
            "09-08", "09-09", "09-10", "09-11", "09-12"
        )

        private val CN_SECTOR_NAMES = listOf("白酒", "新能源", "银行", "医药", "半导体", "地产", "消费", "电力")
        private val HK_SECTOR_NAMES = listOf("科技", "金融", "地产", "消费", "医药", "能源")
        private val US_SECTOR_NAMES = listOf("科技", "消费", "医药", "金融", "能源", "工业")

        private val INTRADAY_LABELS = listOf(
            "09:30", "09:45", "10:00", "10:15", "10:30", "10:45",
            "11:00", "11:15", "11:30", "13:00", "13:15", "13:30",
            "13:45", "14:00", "14:15", "14:30", "14:45", "15:00"
        )

        private val INDEXES = listOf(
            MarketIndex("000001", "上证指数", 3128.46, 0.42, Stock.MARKET_CN),
            MarketIndex("399001", "深证成指", 9842.18, 0.68, Stock.MARKET_CN),
            MarketIndex("399006", "创业板指", 2016.55, -0.35, Stock.MARKET_CN),
            MarketIndex("HSI", "恒生指数", 17852.30, 1.12, Stock.MARKET_HK),
            MarketIndex("IXIC", "纳斯达克", 17840.60, 0.85, Stock.MARKET_US)
        )

        private val ETFS = listOf(
            ETF("510300", "沪深300ETF", 4.12, 4.13, 0.85, ETF.CATEGORY_BROAD, 8_560_000L),
            ETF("510050", "上证50ETF", 2.68, 2.69, 0.42, ETF.CATEGORY_BROAD, 5_120_000L),
            ETF("159915", "创业板ETF", 1.86, 1.85, -0.38, ETF.CATEGORY_BROAD, 6_440_000L),
            ETF("513100", "纳指ETF", 1.52, 1.53, 1.12, ETF.CATEGORY_BROAD, 3_280_000L),
            ETF("512480", "半导体ETF", 1.24, 1.25, 1.68, ETF.CATEGORY_SECTOR, 4_150_000L),
            ETF("159928", "消费ETF", 0.92, 0.92, 0.55, ETF.CATEGORY_SECTOR, 1_860_000L),
            ETF("512000", "券商ETF", 0.78, 0.77, -0.62, ETF.CATEGORY_SECTOR, 2_410_000L),
            ETF("159992", "创新药ETF", 0.64, 0.65, 0.28, ETF.CATEGORY_SECTOR, 1_120_000L),
            ETF("511010", "国债ETF", 118.46, 118.50, 0.08, ETF.CATEGORY_BOND, 980_000L),
            ETF("511260", "十年国债ETF", 127.32, 127.28, -0.05, ETF.CATEGORY_BOND, 640_000L)
        )

        private val FUND_NAMES = listOf(
            "易方达蓝筹精选",
            "华夏成长混合",
            "招商中证白酒",
            "广发稳健增长",
            "南方优质成长",
            "嘉实新能源车",
            "富国天惠成长",
            "兴全合润混合"
        )

        private val SECTORS = listOf(
            Sector("白酒", 1.25, "贵州茅台", "600519", Stock.MARKET_CN),
            Sector("新能源", 2.10, "宁德时代", "300750", Stock.MARKET_CN),
            Sector("银行", 0.46, "招商银行", "600036", Stock.MARKET_CN),
            Sector("医药", -0.38, "恒瑞医药", "600276", Stock.MARKET_CN),
            Sector("半导体", 1.82, "中芯国际", "688981", Stock.MARKET_CN),
            Sector("地产", -1.15, "万科A", "000002", Stock.MARKET_CN),
            Sector("消费", 0.72, "美的集团", "000333", Stock.MARKET_CN),
            Sector("电力", 0.28, "长江电力", "600900", Stock.MARKET_CN),
            Sector("科技", 1.68, "腾讯控股", "00700", Stock.MARKET_HK),
            Sector("金融", 0.55, "汇丰控股", "00005", Stock.MARKET_HK),
            Sector("地产", -0.82, "华润置地", "01109", Stock.MARKET_HK),
            Sector("消费", 0.94, "安踏体育", "02020", Stock.MARKET_HK),
            Sector("医药", -0.26, "药明生物", "02269", Stock.MARKET_HK),
            Sector("能源", 1.05, "中国海洋石油", "00883", Stock.MARKET_HK),
            Sector("科技", 1.42, "苹果", "AAPL", Stock.MARKET_US),
            Sector("消费", 0.38, "沃尔玛", "WMT", Stock.MARKET_US),
            Sector("医药", -0.22, "强生", "JNJ", Stock.MARKET_US),
            Sector("金融", 0.61, "摩根大通", "JPM", Stock.MARKET_US),
            Sector("能源", 0.88, "埃克森美孚", "XOM", Stock.MARKET_US),
            Sector("工业", 0.33, "卡特彼勒", "CAT", Stock.MARKET_US)
        )

        private val CN_SEEDS = listOf(
            Seed("600519", "贵州茅台", 1428.50, 28.6, 8.40, sector = "白酒", marketCap = 17860.0),
            Seed("300750", "宁德时代", 252.30, 22.1, 4.80, sector = "新能源", marketCap = 11240.0),
            Seed("002594", "比亚迪", 268.10, 24.5, 5.10),
            Seed("601318", "中国平安", 52.36, 9.80, 1.12),
            Seed("600036", "招商银行", 38.42, 6.50, 0.98, sector = "银行", marketCap = 9680.0),
            Seed("601166", "兴业银行", 19.18, 5.40, 0.62),
            Seed("000858", "五粮液", 128.60, 18.2, 4.55),
            Seed("600276", "恒瑞医药", 46.28, 48.0, 6.20, sector = "医药", marketCap = 2960.0),
            Seed("300059", "东方财富", 22.15, 32.4, 4.10),
            Seed("601012", "隆基绿能", 15.62, 18.8, 1.85),
            Seed("002475", "立讯精密", 38.90, 21.3, 4.40),
            Seed("000333", "美的集团", 72.35, 14.6, 2.80, sector = "消费", marketCap = 5120.0),
            Seed("600900", "长江电力", 28.44, 16.2, 2.45, sector = "电力", marketCap = 6980.0),
            Seed("601899", "紫金矿业", 16.28, 12.5, 2.90),
            Seed("000001", "平安银行", 11.86, 5.10, 0.58),
            Seed("600030", "中信证券", 26.74, 14.8, 1.35),
            Seed("601398", "工商银行", 6.22, 6.10, 0.55),
            Seed("601288", "农业银行", 5.14, 6.00, 0.52),
            Seed("601988", "中国银行", 4.86, 6.20, 0.50),
            Seed("601857", "中国石油", 8.52, 9.40, 0.88),
            Seed("600028", "中国石化", 6.38, 8.70, 0.72),
            Seed("601088", "中国神华", 38.15, 11.2, 1.45),
            Seed("600887", "伊利股份", 28.06, 17.5, 3.20),
            Seed("000568", "泸州老窖", 132.40, 16.8, 4.10),
            Seed("002304", "洋河股份", 78.22, 15.4, 2.60),
            Seed("600809", "山西汾酒", 185.30, 22.8, 7.10),
            Seed("603288", "海天味业", 38.55, 32.0, 8.20),
            Seed("000651", "格力电器", 42.18, 8.90, 1.95),
            Seed("002415", "海康威视", 32.46, 18.6, 3.40),
            Seed("603259", "药明康德", 55.10, 26.4, 3.80),
            Seed("300015", "爱尔眼科", 14.28, 35.2, 5.60),
            Seed("002371", "北方华创", 328.60, 42.0, 9.50),
            Seed("688981", "中芯国际", 88.40, 55.0, 3.20, sector = "半导体", marketCap = 4120.0),
            Seed("002230", "科大讯飞", 48.75, 68.0, 5.40),
            Seed("300760", "迈瑞医疗", 278.20, 28.5, 6.80),
            Seed("600585", "海螺水泥", 24.16, 9.20, 0.85),
            Seed("601668", "中国建筑", 5.82, 5.40, 0.62),
            Seed("601390", "中国中铁", 6.24, 6.80, 0.70),
            Seed("600048", "保利发展", 8.46, 7.50, 0.68),
            Seed("000002", "万科A", 7.12, 8.10, 0.55, sector = "地产", marketCap = 850.0),
            Seed("601127", "赛力斯", 118.50, 36.0, 8.40),
            Seed("002050", "三花智控", 28.33, 24.2, 5.10),
            Seed("300274", "阳光电源", 72.80, 19.6, 4.20),
            Seed("688111", "金山办公", 268.90, 62.0, 9.80),
            Seed("002460", "赣锋锂业", 36.45, 28.0, 2.40),
            Seed("300014", "亿纬锂能", 42.18, 26.5, 3.10),
            Seed("000725", "京东方A", 4.12, 18.4, 1.05),
            Seed("002241", "歌尔股份", 22.36, 21.0, 2.20),
            Seed("600406", "国电南瑞", 24.58, 22.8, 3.55),
            Seed("601766", "中国中车", 7.64, 16.5, 1.18)
        )

        private val HK_SEEDS = listOf(
            Seed("00700", "腾讯控股", 380.40, 22.5, 3.80, Stock.MARKET_HK, "科技", 32150.0),
            Seed("09988", "阿里巴巴-SW", 82.15, 18.2, 2.10, Stock.MARKET_HK, "科技", 15880.0),
            Seed("03690", "美团-W", 128.60, 35.0, 4.20, Stock.MARKET_HK, "消费", 6240.0),
            Seed("01810", "小米集团-W", 18.42, 24.0, 2.80, Stock.MARKET_HK, "科技", 4680.0),
            Seed("09618", "京东集团-SW", 112.30, 16.5, 2.40, Stock.MARKET_HK, "消费", 3520.0),
            Seed("00005", "汇丰控股", 68.55, 8.40, 0.92, Stock.MARKET_HK, "金融", 12100.0),
            Seed("01299", "友邦保险", 62.80, 14.2, 1.85, Stock.MARKET_HK, "金融", 7120.0),
            Seed("02318", "中国平安", 42.16, 7.80, 0.88, Stock.MARKET_HK, "金融", 7760.0),
            Seed("01109", "华润置地", 28.35, 9.20, 0.76, Stock.MARKET_HK, "地产", 2010.0),
            Seed("01997", "九龙仓置业", 22.48, 8.60, 0.42, Stock.MARKET_HK, "地产", 980.0),
            Seed("02020", "安踏体育", 82.90, 21.4, 4.50, Stock.MARKET_HK, "消费", 1860.0),
            Seed("02331", "李宁", 16.72, 18.6, 2.90, Stock.MARKET_HK, "消费", 430.0),
            Seed("02269", "药明生物", 16.48, 32.0, 3.10, Stock.MARKET_HK, "医药", 680.0),
            Seed("01093", "石药集团", 5.86, 12.5, 1.40, Stock.MARKET_HK, "医药", 980.0),
            Seed("00883", "中国海洋石油", 18.92, 7.20, 1.05, Stock.MARKET_HK, "能源", 8420.0),
            Seed("00857", "中国石油股份", 6.54, 8.10, 0.68, Stock.MARKET_HK, "能源", 12180.0),
            Seed("01024", "快手-W", 48.75, 28.0, 2.20, Stock.MARKET_HK, "科技", 2080.0),
            Seed("09626", "哔哩哔哩-W", 142.20, 42.0, 4.80, Stock.MARKET_HK, "科技", 590.0),
            Seed("09868", "小鹏汽车-W", 42.18, 45.0, 2.60, Stock.MARKET_HK, "科技", 720.0),
            Seed("02015", "理想汽车-W", 88.40, 38.0, 3.40, Stock.MARKET_HK, "科技", 890.0)
        )

        private val US_SEEDS = listOf(
            Seed("AAPL", "苹果", 228.40, 32.5, 48.0, Stock.MARKET_US, "科技", 34800.0),
            Seed("MSFT", "微软", 418.60, 35.2, 12.8, Stock.MARKET_US, "科技", 31200.0),
            Seed("NVDA", "英伟达", 118.75, 52.0, 38.5, Stock.MARKET_US, "科技", 29100.0),
            Seed("GOOGL", "谷歌", 165.20, 24.8, 6.90, Stock.MARKET_US, "科技", 20400.0),
            Seed("AMZN", "亚马逊", 186.40, 41.0, 8.20, Stock.MARKET_US, "消费", 19600.0),
            Seed("META", "Meta", 512.30, 26.4, 8.50, Stock.MARKET_US, "科技", 13100.0),
            Seed("TSLA", "特斯拉", 242.80, 68.0, 14.2, Stock.MARKET_US, "科技", 7760.0),
            Seed("NFLX", "奈飞", 702.15, 38.5, 14.8, Stock.MARKET_US, "消费", 3020.0),
            Seed("AMD", "超威", 156.40, 42.0, 4.60, Stock.MARKET_US, "科技", 2520.0),
            Seed("INTC", "英特尔", 22.85, 18.6, 1.05, Stock.MARKET_US, "科技", 980.0),
            Seed("JPM", "摩根大通", 212.40, 12.2, 1.85, Stock.MARKET_US, "金融", 6120.0),
            Seed("V", "Visa", 278.60, 29.4, 14.5, Stock.MARKET_US, "金融", 5680.0),
            Seed("MA", "万事达", 468.20, 35.8, 58.0, Stock.MARKET_US, "金融", 4320.0),
            Seed("JNJ", "强生", 162.35, 16.8, 5.40, Stock.MARKET_US, "医药", 3920.0),
            Seed("UNH", "联合健康", 562.10, 21.5, 5.80, Stock.MARKET_US, "医药", 5180.0),
            Seed("WMT", "沃尔玛", 78.42, 28.6, 6.20, Stock.MARKET_US, "消费", 6320.0),
            Seed("KO", "可口可乐", 68.15, 24.0, 10.8, Stock.MARKET_US, "消费", 2940.0),
            Seed("XOM", "埃克森美孚", 118.60, 13.4, 1.85, Stock.MARKET_US, "能源", 4980.0),
            Seed("CVX", "雪佛龙", 148.20, 14.8, 1.72, Stock.MARKET_US, "能源", 2720.0),
            Seed("CAT", "卡特彼勒", 352.80, 16.2, 8.40, Stock.MARKET_US, "工业", 1740.0)
        )

        private fun Double.round2(): Double = round(this * 100.0) / 100.0

        private fun Double.px(): String {
            val cents = round(this * 100.0).toLong()
            val negative = cents < 0
            val abs = abs(cents)
            val text = "${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
            return if (negative) "-$text" else text
        }
    }
}
