package com.example.myandroidapplication.data.model

/**
 * 产业链关联公司。涨跌幅须与 [com.example.myandroidapplication.data.repository.StockRepository.getStock] 一致。
 *
 * @property name 证券简称
 * @property symbol 证券代码
 * @property relation 上游 / 下游
 * @property changePercent 涨跌幅百分比数值
 */
data class ChainPeer(
    val name: String,
    val symbol: String,
    val relation: String,
    val changePercent: Double
) {
    companion object {
        const val UPSTREAM = "上游"
        const val DOWNSTREAM = "下游"
    }
}
