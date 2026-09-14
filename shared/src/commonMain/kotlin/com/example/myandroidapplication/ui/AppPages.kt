package com.example.myandroidapplication.ui

/**
 * Kuikly `@Page` 名称与路由参数。改名会断跳转，Phase 14 保持不变。
 */
object AppPages {
    /** 首页行情列表。 */
    const val QUOTE_LIST = "quote_list"
    /** 个股详情。 */
    const val STOCK_DETAIL = "stock_detail"
    /** 自选列表。 */
    const val WATCHLIST = "watchlist"
    /** 选股分类。 */
    const val STOCK_PICKER = "stock_picker"
    /** 基金榜单。 */
    const val RANKINGS = "rankings"
    /** 多股对比。 */
    const val COMPARE = "compare"
    /** 详情页股票代码参数。 */
    const val ARG_SYMBOL = "symbol"
}
