package com.example.myandroidapplication.ui.page

import androidx.compose.runtime.Composable
import com.example.myandroidapplication.data.AppContainer
import com.example.myandroidapplication.data.model.Stock
import com.example.myandroidapplication.ui.AppPages
import com.example.myandroidapplication.ui.component.QuoteTopBar
import com.example.myandroidapplication.ui.component.StockCard
import com.example.myandroidapplication.ui.theme.AppColors
import com.example.myandroidapplication.ui.theme.AppDimens
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 首页行情列表。数据来自 [AppContainer.stockRepository]。
 */
@Page(AppPages.QUOTE_LIST)
internal class QuoteListPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        val statusBarDp = pagerData.statusBarHeight.dp
        val stocks = AppContainer.stockRepository.getStocks()
        setContent {
            QuoteListScreen(
                stocks = stocks,
                statusBarHeight = statusBarDp,
                onStockClick = { symbol ->
                    acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(
                        AppPages.STOCK_DETAIL,
                        JSONObject().put(AppPages.ARG_SYMBOL, symbol)
                    )
                }
            )
        }
    }
}

@Composable
private fun QuoteListScreen(
    stocks: List<Stock>,
    statusBarHeight: Dp,
    onStockClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgPage)
    ) {
        QuoteTopBar(
            title = "行情",
            statusBarHeight = statusBarHeight,
            titleCentered = false
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AppDimens.Space4,
                end = AppDimens.Space4,
                top = AppDimens.Space2,
                bottom = AppDimens.Space6
            ),
            beyondBoundsItemCount = 3
        ) {
            items(
                items = stocks,
                key = { it.symbol },
                contentType = { "stock_card" }
            ) { stock ->
                StockCard(
                    stock = stock,
                    onClick = { onStockClick(stock.symbol) }
                )
            }
        }
    }
}
