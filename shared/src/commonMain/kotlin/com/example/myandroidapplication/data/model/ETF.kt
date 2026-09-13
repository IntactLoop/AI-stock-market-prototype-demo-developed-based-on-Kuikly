package com.example.myandroidapplication.data.model

/**
 * ETF 独立模型，不塞进 [Stock]。无涨跌额，用净值 / IOPV。
 *
 * @property symbol 基金代码
 * @property name 名称
 * @property nav 净值
 * @property iopv IOPV
 * @property changePercent 涨跌幅（百分比数值）
 * @property category 宽基 / 行业 / 债券
 * @property volume 成交量
 */
data class ETF(
    val symbol: String,
    val name: String,
    val nav: Double,
    val iopv: Double,
    val changePercent: Double,
    val category: String,
    val volume: Long
) {
    companion object {
        const val CATEGORY_BROAD = "宽基"
        const val CATEGORY_SECTOR = "行业"
        const val CATEGORY_BOND = "债券"
    }
}

/**
 * 选股页四个分类，对应 [com.example.myandroidapplication.data.repository.StockRepository.getStockPicks]。
 */
object StockPickCategory {
    const val COMPANY = "选公司"
    const val ETF = "选ETF"
    const val INSTITUTION = "跟机构"
    const val CAPITAL = "跟资金"

    val ALL = listOf(COMPANY, ETF, INSTITUTION, CAPITAL)
}
