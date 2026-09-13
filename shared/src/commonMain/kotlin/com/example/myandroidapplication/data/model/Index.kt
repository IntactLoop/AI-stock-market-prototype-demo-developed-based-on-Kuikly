package com.example.myandroidapplication.data.model

/**
 * 市场指数。供首页 [IndexCard] 横滑使用。
 *
 * @property symbol 指数代码
 * @property name 指数名称
 * @property price 最新点位
 * @property changePercent 涨跌幅（百分比数值）
 * @property market 所属市场，取值与 [Stock.MARKET_CN] 等一致
 */
data class MarketIndex(
    val symbol: String,
    val name: String,
    val price: Double,
    val changePercent: Double,
    val market: String
)

/**
 * 板块表现。供首页 [SectorRow] 横滑使用。
 *
 * @property name 板块名称
 * @property changePercent 板块涨跌幅（百分比数值）
 * @property leadStockName 领涨股简称
 * @property leadStockSymbol 领涨股代码
 * @property market 所属市场
 */
data class Sector(
    val name: String,
    val changePercent: Double,
    val leadStockName: String,
    val leadStockSymbol: String,
    val market: String
)
