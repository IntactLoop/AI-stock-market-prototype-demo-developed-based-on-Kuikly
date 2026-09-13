package com.example.myandroidapplication.data.model

/**
 * 单只股票的基金持仓明细。
 *
 * @property fundName 基金名称
 * @property symbol 对应股票代码
 * @property holdingRatio 持仓占比（百分比数值）
 * @property holdingChange 持仓变动（百分点）
 * @property marketValue 持仓市值（亿元）
 */
data class FundHolding(
    val fundName: String,
    val symbol: String,
    val holdingRatio: Double,
    val holdingChange: Double,
    val marketValue: Double
)
