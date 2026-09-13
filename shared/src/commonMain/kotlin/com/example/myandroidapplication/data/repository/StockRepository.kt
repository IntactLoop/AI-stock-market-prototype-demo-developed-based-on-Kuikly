package com.example.myandroidapplication.data.repository

import com.example.myandroidapplication.data.model.AdjustType
import com.example.myandroidapplication.data.model.CandlePoint
import com.example.myandroidapplication.data.model.ChartPeriod
import com.example.myandroidapplication.data.model.ChartType
import com.example.myandroidapplication.data.model.ETF
import com.example.myandroidapplication.data.model.FundHolding
import com.example.myandroidapplication.data.model.MarketIndex
import com.example.myandroidapplication.data.model.Ranking
import com.example.myandroidapplication.data.model.Sector
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.data.model.StockFilter

/**
 * 行情数据访问接口。UI 层只依赖本接口，不依赖 Mock 实现。
 *
 * [getStocks] / [getStock] 签名冻结。多市场与图表数据走下方新增方法。
 */
interface StockRepository {
    /**
     * 返回首页默认行情列表（沪深 A 股），顺序稳定。
     */
    fun getStocks(): List<Stock>

    /**
     * 按证券代码查询单只股票（全市场）；不存在时返回 null。
     */
    fun getStock(symbol: String): Stock?

    /**
     * 市场指数。[market] 为 null 时返回全部。
     */
    fun getIndexes(market: String? = null): List<MarketIndex>

    /**
     * 指定市场的股票列表。
     */
    fun getStocksByMarket(market: String): List<Stock>

    /**
     * 板块列表。[market] 为 null 时返回全部。
     */
    fun getSectors(market: String? = null): List<Sector>

    /**
     * 指定市场按人气从高到低的热门股。
     */
    fun getHotStocks(market: String): List<Stock>

    /**
     * 多周期 / 类型 / 复权后的 K 线点列。无该股票时返回空列表。
     */
    fun getChartData(
        symbol: String,
        period: ChartPeriod,
        type: ChartType,
        adjust: AdjustType
    ): List<CandlePoint>

    /**
     * 先按市场再按 [criteria] 过滤。空条件返回该市场全量，顺序与 [getStocksByMarket] 一致。
     * 排序由 UI 在结果上做，本方法不排序。
     */
    fun filterStocks(market: String, criteria: StockFilter): List<Stock>

    /**
     * 选股分类池。[category] 为 [com.example.myandroidapplication.data.model.StockPickCategory]
     * 中的选公司 / 跟机构 / 跟资金。选 ETF 返回空列表，改走 [getETFs]。
     */
    fun getStockPicks(category: String): List<Stock>

    /**
     * ETF 列表。模型与 [Stock] 不同，含净值 / IOPV。
     */
    fun getETFs(): List<ETF>

    /**
     * 基金榜单。[type] 为 [com.example.myandroidapplication.data.model.RankingType] 四类之一。
     */
    fun getRankings(type: String): List<Ranking>

    /**
     * 指定股票的基金持仓明细。无该股票时返回空列表。
     */
    fun getFundHoldings(symbol: String): List<FundHolding>
}
