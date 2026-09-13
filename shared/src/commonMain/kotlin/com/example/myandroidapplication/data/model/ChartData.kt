package com.example.myandroidapplication.data.model

/**
 * 走势图周期。标签供详情页 PeriodTab 使用。
 */
enum class ChartPeriod(val label: String) {
    INTRADAY("分时"),
    FIVE_DAY("五日"),
    DAILY("日K"),
    WEEKLY("周K"),
    MONTHLY("月K")
}

/**
 * 主图绘制类型。
 */
enum class ChartType(val label: String) {
    LINE("折线"),
    CANDLE("K线")
}

/**
 * 复权方式。Mock 用价格缩放近似，不模拟真实分红。
 */
enum class AdjustType(val label: String) {
    NONE("不复权"),
    FORWARD("前复权"),
    BACKWARD("后复权")
}

/**
 * 一根 K 线（或分时采样点的 OHLC 展开）。
 *
 * @property time 横轴标签
 * @property open 开盘
 * @property high 最高
 * @property low 最低
 * @property close 收盘
 * @property volume 成交量
 */
data class CandlePoint(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
