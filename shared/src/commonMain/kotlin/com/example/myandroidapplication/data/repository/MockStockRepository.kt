package com.example.myandroidapplication.data.repository

import com.example.myandroidapplication.data.model.ChartPoint
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.model.WatchAlert
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sin

/**
 * 预生成 50 条 A 股 Mock。每条都填满 AI 字段，且建议 / 信号 / 风险与价格结构一致。
 *
 * 三种情景均覆盖：
 * - 趋势向上 + 接近支撑 → 买入
 * - 震荡 + 接近压力 → 观望
 * - 趋势向下 + 跌破支撑 → 卖出
 */
class MockStockRepository : StockRepository {

    private val cachedStocks: List<Stock> by lazy { SEEDS.mapIndexed { index, seed -> buildStock(seed, index) } }

    override fun getStocks(): List<Stock> = cachedStocks

    override fun getStock(symbol: String): Stock? = cachedStocks.find { it.symbol == symbol }

    private data class Seed(
        val symbol: String,
        val name: String,
        val basePrice: Double,
        val pe: Double,
        val pb: Double
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
            chartPoints = buildChartPoints(price, volume, scenario, index)
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

        private val SEEDS = listOf(
            Seed("600519", "贵州茅台", 1428.50, 28.6, 8.40),
            Seed("300750", "宁德时代", 252.30, 22.1, 4.80),
            Seed("002594", "比亚迪", 268.10, 24.5, 5.10),
            Seed("601318", "中国平安", 52.36, 9.80, 1.12),
            Seed("600036", "招商银行", 38.42, 6.50, 0.98),
            Seed("601166", "兴业银行", 19.18, 5.40, 0.62),
            Seed("000858", "五粮液", 128.60, 18.2, 4.55),
            Seed("600276", "恒瑞医药", 46.28, 48.0, 6.20),
            Seed("300059", "东方财富", 22.15, 32.4, 4.10),
            Seed("601012", "隆基绿能", 15.62, 18.8, 1.85),
            Seed("002475", "立讯精密", 38.90, 21.3, 4.40),
            Seed("000333", "美的集团", 72.35, 14.6, 2.80),
            Seed("600900", "长江电力", 28.44, 16.2, 2.45),
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
            Seed("688981", "中芯国际", 88.40, 55.0, 3.20),
            Seed("002230", "科大讯飞", 48.75, 68.0, 5.40),
            Seed("300760", "迈瑞医疗", 278.20, 28.5, 6.80),
            Seed("600585", "海螺水泥", 24.16, 9.20, 0.85),
            Seed("601668", "中国建筑", 5.82, 5.40, 0.62),
            Seed("601390", "中国中铁", 6.24, 6.80, 0.70),
            Seed("600048", "保利发展", 8.46, 7.50, 0.68),
            Seed("000002", "万科A", 7.12, 8.10, 0.55),
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
