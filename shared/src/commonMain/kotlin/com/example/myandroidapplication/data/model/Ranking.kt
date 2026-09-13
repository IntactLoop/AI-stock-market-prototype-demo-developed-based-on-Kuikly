package com.example.myandroidapplication.data.model

/**
 * 基金榜单条目。持仓占比与持仓变动均为数值字段，UI 只做格式化，不临时拼业务文案。
 *
 * @property rank 名次，从 1 起
 * @property symbol 证券代码
 * @property name 证券简称
 * @property holdingRatio 持仓占比（百分比数值，如 8.50 表示 8.50%）
 * @property holdingChange 持仓变动（百分点，正为加仓）
 */
data class Ranking(
    val rank: Int,
    val symbol: String,
    val name: String,
    val holdingRatio: Double,
    val holdingChange: Double
)

/**
 * 四个榜单类型，对应 [com.example.myandroidapplication.data.repository.StockRepository.getRankings]。
 */
object RankingType {
    const val FUND_HEAVY = "基金重仓股"
    const val PUBLIC_HEAVY = "公募重仓"
    const val PUBLIC_INCREASE = "加仓榜"
    const val PUBLIC_NEW = "新进榜"

    val ALL = listOf(FUND_HEAVY, PUBLIC_HEAVY, PUBLIC_INCREASE, PUBLIC_NEW)
}
