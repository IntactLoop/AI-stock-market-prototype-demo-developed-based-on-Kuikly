package com.example.myandroidapplication.data

import com.example.myandroidapplication.data.repository.MockStockRepository
import com.example.myandroidapplication.data.repository.StockRepository

/**
 * 组合根。页面与组件通过 [stockRepository] 取数，避免直接构造 [MockStockRepository]。
 */
object AppContainer {
    val stockRepository: StockRepository = MockStockRepository()
}
