package com.example.myandroidapplication.data.repository

import com.example.myandroidapplication.data.model.Stock

/**
 * 行情数据访问接口。UI 层只依赖本接口，不依赖 Mock 实现。
 */
interface StockRepository {
    /**
     * 返回首页行情列表，顺序稳定。
     */
    fun getStocks(): List<Stock>

    /**
     * 按证券代码查询单只股票；不存在时返回 null。
     */
    fun getStock(symbol: String): Stock?
}
