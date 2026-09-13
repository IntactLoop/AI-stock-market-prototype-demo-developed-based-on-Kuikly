package com.example.myandroidapplication.data.model

/**
 * A 股行情与 AI 分析的聚合模型。
 *
 * 字段与 `.cursor/workflow-state.md` 3.4 节对齐，供列表、详情和全部 AI 模块共用。
 *
 * @property symbol 证券代码，如 `600519`
 * @property name 证券简称，如 `贵州茅台`
 * @property price 最新价
 * @property change 涨跌额
 * @property changePercent 涨跌幅（百分比数值，1.25 表示 +1.25%）
 * @property open 今开
 * @property high 最高
 * @property low 最低
 * @property close 最新/收盘
 * @property volume 成交量（股）
 * @property turnover 成交额（元）
 * @property pe 市盈率
 * @property pb 市净率
 * @property ma5 5 日均线
 * @property ma10 10 日均线
 * @property ma20 20 日均线
 * @property macd MACD 柱/差值
 * @property rsi RSI(14)
 * @property support 支撑位
 * @property resistance 压力位
 * @property aiSignal 主技术信号（与 [signals] 首项一致）
 * @property aiConfidence 信号置信度，范围 0–100
 * @property aiAdvice 买卖建议：买入 / 观望 / 卖出
 * @property riskLevel 风险等级：低风险 / 中风险 / 高风险
 * @property buyPoint 建议支撑买入价
 * @property sellPoint 建议压力卖出价
 * @property aiSummary 当日行情总结
 * @property volumeRatio 量比，>1 表示放量
 * @property signals 技术信号标签列表，供 [SignalBadge] 使用
 * @property aiInterpretation 信号解读正文
 * @property adviceReason 买卖建议理由
 * @property shortTrend 短期趋势文案
 * @property midTrend 中期趋势文案
 * @property alerts 盯盘提醒，至少 3 条
 * @property chartPoints 走势点，供 StockChart 与图表联动
 */
data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val turnover: Double,
    val pe: Double,
    val pb: Double,
    val ma5: Double,
    val ma10: Double,
    val ma20: Double,
    val macd: Double,
    val rsi: Double,
    val support: Double,
    val resistance: Double,
    val aiSignal: String,
    val aiConfidence: Int,
    val aiAdvice: String,
    val riskLevel: String,
    val buyPoint: Double,
    val sellPoint: Double,
    val aiSummary: String,
    val volumeRatio: Double,
    val signals: List<String>,
    val aiInterpretation: String,
    val adviceReason: String,
    val shortTrend: String,
    val midTrend: String,
    val alerts: List<WatchAlert>,
    val chartPoints: List<ChartPoint>
) {
    companion object {
        const val ADVICE_BUY = "买入"
        const val ADVICE_HOLD = "观望"
        const val ADVICE_SELL = "卖出"
        const val RISK_LOW = "低风险"
        const val RISK_MID = "中风险"
        const val RISK_HIGH = "高风险"
        const val ALERT_RISK = "风险"
        const val ALERT_FUND = "资金"
        const val ALERT_TREND = "趋势"
    }
}

/**
 * 走势图上的一个时间点。
 *
 * @property time 横轴标签，如 `09-12`
 * @property price 该时点价格
 * @property volume 该时点成交量
 */
data class ChartPoint(
    val time: String,
    val price: Double,
    val volume: Long
)

/**
 * AI 盯盘提醒一条，对应设计系统 AITicker。
 *
 * @property type 风险 / 资金 / 趋势，决定竖条与圆点颜色
 * @property message 单行提醒文案，需含具体点位或百分比
 */
data class WatchAlert(
    val type: String,
    val message: String
)
