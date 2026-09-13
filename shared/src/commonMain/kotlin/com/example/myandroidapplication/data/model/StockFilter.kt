package com.example.myandroidapplication.data.model

/**
 * 首页自定义筛选条件。空区间 / 空标签表示不限制该项。
 *
 * 标签为多选且须全部命中（AND）。市值单位与 [Stock.marketCap] 一致，为亿元，不从成交额反推。
 */
data class StockFilter(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minChangePercent: Double? = null,
    val maxChangePercent: Double? = null,
    val minMarketCap: Double? = null,
    val maxMarketCap: Double? = null,
    val tags: Set<String> = emptySet()
) {
    fun isEmpty(): Boolean =
        minPrice == null &&
            maxPrice == null &&
            minChangePercent == null &&
            maxChangePercent == null &&
            minMarketCap == null &&
            maxMarketCap == null &&
            tags.isEmpty()

    fun matches(stock: Stock): Boolean {
        if (minPrice != null && stock.price < minPrice) return false
        if (maxPrice != null && stock.price > maxPrice) return false
        if (minChangePercent != null && stock.changePercent < minChangePercent) return false
        if (maxChangePercent != null && stock.changePercent > maxChangePercent) return false
        if (minMarketCap != null && stock.marketCap < minMarketCap) return false
        if (maxMarketCap != null && stock.marketCap > maxMarketCap) return false
        if (tags.isNotEmpty() && !stock.tags.containsAll(tags)) return false
        return true
    }

    companion object {
        val EMPTY = StockFilter()
    }
}
