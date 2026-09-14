package com.example.myandroidapplication.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.repository.MockStockRepository
import com.example.myandroidapplication.data.repository.MockWatchlistRepository
import com.example.myandroidapplication.data.repository.StockRepository
import com.example.myandroidapplication.data.repository.WatchlistRepository

/**
 * 组合根。页面与组件通过 [stockRepository] / [watchlistRepository] 取数，避免直接构造 Mock。
 * 对比勾选放在顶层，供列表页与对比页共享；Compose 用 snapshot 列表驱动重组。
 */
object AppContainer {
    const val COMPARE_MAX = 4

    val stockRepository: StockRepository = MockStockRepository()
    val watchlistRepository: WatchlistRepository = MockWatchlistRepository()

    var compareMode by mutableStateOf(false)
        private set

    val compareSymbols = mutableStateListOf<String>()

    fun enterCompareMode() {
        compareMode = true
    }

    fun exitCompareMode() {
        compareMode = false
        compareSymbols.clear()
    }

    fun toggleCompareSymbol(symbol: String) {
        if (!compareMode) return
        if (compareSymbols.contains(symbol)) {
            compareSymbols.remove(symbol)
        } else if (compareSymbols.size < COMPARE_MAX) {
            compareSymbols.add(symbol)
        }
    }

    /**
     * 按勾选顺序解析股票，最多 [COMPARE_MAX] 只。
     */
    fun resolvedCompareStocks(): List<Stock> {
        return compareSymbols
            .distinct()
            .take(COMPARE_MAX)
            .mapNotNull { symbol -> stockRepository.getStock(symbol) }
    }

    const val RISK_STEADY = "稳健"
    const val RISK_BALANCED = "平衡"
    const val RISK_AGGRESSIVE = "进取"

    var riskPreference by mutableStateOf(RISK_BALANCED)
        private set

    var riskPreferenceChosen by mutableStateOf(false)
        private set

    var riskProfilePrompted by mutableStateOf(false)
        private set

    fun chooseRiskPreference(value: String) {
        riskPreference = value
        riskPreferenceChosen = true
        riskProfilePrompted = true
    }

    fun markRiskProfilePrompted() {
        riskProfilePrompted = true
    }

    /**
     * 本次进程是否已自动弹出过盘前早报。只作一次性标记，不保存 Sheet 可见状态。
     */
    var briefingAutoShownThisSession by mutableStateOf(false)
        private set

    fun markBriefingAutoShown() {
        briefingAutoShownThisSession = true
    }
}
